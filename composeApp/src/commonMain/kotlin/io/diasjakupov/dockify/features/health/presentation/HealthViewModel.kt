package io.diasjakupov.dockify.features.health.presentation

import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.auth.domain.usecase.GetCurrentUserUseCase
import io.diasjakupov.dockify.features.health.domain.model.HealthData
import io.diasjakupov.dockify.features.health.domain.model.HealthMetric
import io.diasjakupov.dockify.features.health.domain.model.HealthMetricType
import io.diasjakupov.dockify.features.health.domain.repository.HealthRepository
import io.diasjakupov.dockify.features.health.domain.usecase.CheckHealthPermissionsUseCase
import io.diasjakupov.dockify.features.health.domain.usecase.ReadPlatformHealthDataUseCase
import io.diasjakupov.dockify.features.health.domain.usecase.SyncHealthDataUseCase
import io.diasjakupov.dockify.features.health.permission.HealthPermissionHandler
import io.diasjakupov.dockify.features.location.domain.model.Location
import io.diasjakupov.dockify.features.location.domain.usecase.GetCurrentLocationUseCase
import io.diasjakupov.dockify.features.location.permission.LocationPermissionHandler
import io.diasjakupov.dockify.features.recommendation.domain.usecase.GetRecommendationUseCase
import io.diasjakupov.dockify.ui.base.BaseViewModel
import io.diasjakupov.dockify.ui.base.LoadingState
import io.diasjakupov.dockify.ui.base.toUserMessage
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * ViewModel for the Health screen.
 * Manages health metrics display, sync operations, and recommendations.
 *
 * Auto-sync flow: On screen load, reads from Health Connect/HealthKit,
 * displays data immediately (optimistic update), then syncs to backend in background.
 */
@OptIn(ExperimentalTime::class)
class HealthViewModel(
    private val syncHealthDataUseCase: SyncHealthDataUseCase,
    private val readPlatformHealthDataUseCase: ReadPlatformHealthDataUseCase,
    private val checkHealthPermissionsUseCase: CheckHealthPermissionsUseCase,
    private val getRecommendationUseCase: GetRecommendationUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val healthRepository: HealthRepository,
    private val healthPermissionHandler: HealthPermissionHandler,
    private val locationPermissionHandler: LocationPermissionHandler
) : BaseViewModel<HealthState, HealthAction, HealthEffect>(HealthState()) {

    private val allMetricTypes = HealthMetricType.entries.toList()

    init {
        onAction(HealthAction.CheckPermissionsAndAutoSync)
    }

    override fun handleAction(action: HealthAction) {
        when (action) {
            is HealthAction.LoadHealthData -> checkPermissionsAndAutoSync() // Redirect to platform read flow
            is HealthAction.SyncHealthData -> syncHealthData()
            is HealthAction.RequestPermissions -> requestPermissions()
            is HealthAction.PermissionGranted -> handlePermissionGranted()
            is HealthAction.PermissionDenied -> handlePermissionDenied()
            is HealthAction.RefreshRecommendation -> loadRecommendation()
            is HealthAction.DismissError -> updateState { copy(error = null) }
            is HealthAction.RetryLastAction -> retryPlatformRead() // Retry platform read, not GET
            is HealthAction.CheckPermissionsAndAutoSync -> checkPermissionsAndAutoSync()
            is HealthAction.DismissBackgroundSyncError -> updateState { copy(backgroundSyncError = null) }
            is HealthAction.RetryBackgroundSync -> retryBackgroundSync()
        }
    }

    /**
     * Retries reading from platform health data source.
     * Resets the flag to allow re-reading and triggers the auto-sync flow.
     */
    private fun retryPlatformRead() {
        updateState { copy(hasInitiallyLoadedPlatformData = false, error = null) }
        checkPermissionsAndAutoSync()
    }

    private fun syncHealthData() {
        if (!currentState.canSync) return

        updateState { copy(isSyncing = true) }

        launch(
            onError = { e ->
                updateState {
                    copy(
                        isSyncing = false,
                        error = e.message ?: "Sync failed"
                    )
                }
            }
        ) {
            val userId = getCurrentUserId() ?: run {
                updateState { copy(isSyncing = false) }
                return@launch
            }

            val location = getLocationSilently()

            when (val result = syncHealthDataUseCase(userId, allMetricTypes, location)) {
                is Resource.Success -> {
                    updateState {
                        copy(
                            isSyncing = false,
                            lastSyncTimestamp = Clock.System.now().toEpochMilliseconds()
                        )
                    }
                    emitEffect(HealthEffect.SyncSuccess)
                    emitEffect(HealthEffect.ShowSnackbar("Health data synced successfully"))
                    // Data is already displayed from platform - no GET needed
                }
                is Resource.Error -> {
                    val errorMessage = result.error.toUserMessage()
                    updateState {
                        copy(
                            isSyncing = false,
                            error = errorMessage
                        )
                    }
                    emitEffect(HealthEffect.ShowSnackbar(errorMessage))
                }
            }
        }
    }

    private fun requestPermissions() {
        updateState { copy(loadingState = LoadingState.LOADING) }

        launch(
            onError = { e ->
                updateState {
                    copy(
                        permissionState = PermissionState.Denied,
                        loadingState = LoadingState.IDLE,
                        error = e.message
                    )
                }
            }
        ) {
            // Request permission - this will show the system dialog and suspend until user responds
            val granted = healthPermissionHandler.requestHealthPermissions()

            if (granted) {
                updateState {
                    copy(
                        permissionState = PermissionState.Granted,
                        hasInitiallyLoadedPlatformData = false // Reset to allow auto-sync
                    )
                }
                // Trigger auto-sync after permission granted
                onAction(HealthAction.CheckPermissionsAndAutoSync)
            } else {
                updateState {
                    copy(
                        permissionState = PermissionState.Denied,
                        loadingState = LoadingState.IDLE
                    )
                }
                emitEffect(HealthEffect.ShowSnackbar("Health permissions denied"))
            }
        }
    }

    private fun handlePermissionGranted() {
        updateState {
            copy(
                permissionState = PermissionState.Granted,
                hasInitiallyLoadedPlatformData = false // Reset to allow auto-sync
            )
        }
        // Trigger auto-sync after permission granted
        onAction(HealthAction.CheckPermissionsAndAutoSync)
    }

    private fun handlePermissionDenied() {
        updateState { copy(permissionState = PermissionState.Denied) }
        emitEffect(HealthEffect.ShowSnackbar("Health permissions denied"))
    }

    private fun loadRecommendation() {
        updateState { copy(isRecommendationLoading = true) }

        launch(
            onError = {
                updateState { copy(isRecommendationLoading = false) }
            }
        ) {
            when (val result = getRecommendationUseCase()) {
                is Resource.Success -> {
                    updateState {
                        copy(
                            recommendation = result.data,
                            isRecommendationLoading = false
                        )
                    }
                }
                is Resource.Error -> {
                    updateState { copy(isRecommendationLoading = false) }
                }
            }
        }
    }

    private suspend fun getCurrentUserId(): String? {
        return when (val result = getCurrentUserUseCase()) {
            is Resource.Success -> result.data.id
            is Resource.Error -> {
                updateState {
                    copy(
                        loadingState = LoadingState.IDLE,
                        error = "Please log in to view health data"
                    )
                }
                null
            }
        }
    }

    /**
     * Attempts to get the current location silently.
     * Returns null if location is unavailable (permission denied, GPS off, etc.)
     * Does not block or show errors - location is optional for health sync.
     */
    private suspend fun getLocationSilently(): Location? {
        return when (val result = getCurrentLocationUseCase()) {
            is Resource.Success -> result.data
            is Resource.Error -> null
        }
    }

    /**
     * Requests location permission in the background.
     * Does not block health data flow - location is optional.
     * Called after health permission is granted.
     */
    private fun requestLocationPermissionSilently() {
        launch {
            if (!locationPermissionHandler.hasLocationPermission()) {
                locationPermissionHandler.requestLocationPermission()
            }
        }
    }

    /**
     * Checks permissions and automatically syncs health data on screen load.
     * This is the main entry point for the auto-sync flow.
     *
     * Flow:
     * 1. Check if platform (Health Connect/HealthKit) is available
     * 2. If permissions not granted -> request permissions first
     * 3. After permissions granted -> read from platform and sync to backend
     */
    private fun checkPermissionsAndAutoSync() {
        // Prevent duplicate auto-syncs
        if (currentState.hasInitiallyLoadedPlatformData) {
            return
        }

        updateState { copy(loadingState = LoadingState.LOADING) }

        launch(
            onError = { e ->
                updateState {
                    copy(
                        loadingState = LoadingState.IDLE,
                        error = e.message ?: "Failed to check permissions",
                        hasInitiallyLoadedPlatformData = true
                    )
                }
            }
        ) {
            val userId = getCurrentUserId() ?: return@launch

            // Step 1: Check if platform is available
            val isAvailable = checkHealthPermissionsUseCase.isPlatformAvailable()
            if (!isAvailable) {
                updateState {
                    copy(
                        permissionState = PermissionState.NotAvailable,
                        isPlatformHealthAvailable = false,
                        loadingState = LoadingState.IDLE,
                        hasInitiallyLoadedPlatformData = true,
                        error = "Health Connect/HealthKit is not available on this device"
                    )
                }
                // No fallback to GET - platform is required
                return@launch
            }

            // Step 2: Check permissions
            val hasPermissions = checkHealthPermissionsUseCase.hasPermissions(allMetricTypes)

            if (hasPermissions) {
                // Permissions already granted - proceed with auto-sync
                updateState { copy(permissionState = PermissionState.Granted) }
                // Request location permission (non-blocking, in background)
                requestLocationPermissionSilently()
                autoSyncWithOptimisticUpdate(userId)
            } else {
                // Permissions not granted - request them first
                updateState {
                    copy(
                        permissionState = PermissionState.Unknown,
                        loadingState = LoadingState.IDLE
                    )
                }
                // Request permissions - this will trigger the system permission dialog
                requestPermissionsAndThenSync(userId)
            }
        }
    }

    /**
     * Requests health permissions and then syncs data after permission is granted.
     * Called on initial screen load when permissions are not yet granted.
     */
    private fun requestPermissionsAndThenSync(userId: String) {
        launch(
            onError = { e ->
                updateState {
                    copy(
                        permissionState = PermissionState.Denied,
                        loadingState = LoadingState.IDLE,
                        hasInitiallyLoadedPlatformData = true,
                        error = e.message ?: "Failed to request permissions"
                    )
                }
            }
        ) {
            // Request permission - this will show the system dialog and suspend until user responds
            val granted = healthPermissionHandler.requestHealthPermissions()

            if (granted) {
                updateState { copy(permissionState = PermissionState.Granted) }
                // Request location permission (non-blocking, in background)
                requestLocationPermissionSilently()
                // Permissions granted - proceed with auto-sync
                autoSyncWithOptimisticUpdate(userId)
            } else {
                updateState {
                    copy(
                        permissionState = PermissionState.Denied,
                        loadingState = LoadingState.IDLE,
                        hasInitiallyLoadedPlatformData = true
                    )
                }
            }
        }
    }

    /**
     * Reads from Health Connect/HealthKit and displays data immediately,
     * then syncs to backend in background.
     */
    private fun autoSyncWithOptimisticUpdate(userId: String) {
        launch(
            onError = { e ->
                updateState {
                    copy(
                        loadingState = LoadingState.IDLE,
                        error = e.message ?: "Failed to read health data",
                        hasInitiallyLoadedPlatformData = true
                    )
                }
            }
        ) {
            // Step 1: Read from platform (Health Connect/HealthKit)
            when (val platformResult = readPlatformHealthDataUseCase(allMetricTypes)) {
                is Resource.Success -> {
                    val platformMetrics = platformResult.data

                    // Step 2: OPTIMISTIC UPDATE - Display platform data immediately
                    updateState {
                        copy(
                            healthMetrics = platformMetrics,
                            loadingState = LoadingState.IDLE,
                            hasInitiallyLoadedPlatformData = true,
                            isBackgroundSyncing = true,
                            backgroundSyncError = null
                        )
                    }

                    // Load recommendations based on platform data
                    loadRecommendation()

                    // Step 3: Upload to backend in background
                    syncToBackendInBackground(userId, platformMetrics)
                }
                is Resource.Error -> {
                    updateState {
                        copy(
                            loadingState = LoadingState.IDLE,
                            error = platformResult.error.toUserMessage(),
                            hasInitiallyLoadedPlatformData = true
                        )
                    }
                    // No fallback to GET - show error with retry option
                }
            }
        }
    }

    /**
     * Syncs health metrics to the backend in background.
     * Does not block UI or show loading indicators - the data is already displayed.
     */
    private fun syncToBackendInBackground(userId: String, metrics: List<HealthMetric>) {
        launch(
            onError = { e ->
                updateState {
                    copy(
                        isBackgroundSyncing = false,
                        backgroundSyncError = e.message ?: "Failed to sync to server"
                    )
                }
                emitEffect(HealthEffect.BackgroundSyncFailed)
            }
        ) {
            val location = getLocationSilently()

            val healthData = HealthData(
                userId = userId,
                metrics = metrics,
                location = location
            )

            when (val result = healthRepository.syncHealthData(healthData)) {
                is Resource.Success -> {
                    updateState {
                        copy(
                            isBackgroundSyncing = false,
                            backgroundSyncError = null,
                            lastSyncTimestamp = Clock.System.now().toEpochMilliseconds()
                        )
                    }
                }
                is Resource.Error -> {
                    updateState {
                        copy(
                            isBackgroundSyncing = false,
                            backgroundSyncError = result.error.toUserMessage()
                        )
                    }
                    emitEffect(HealthEffect.BackgroundSyncFailed)
                }
            }
        }
    }

    /**
     * Retries background sync without re-reading from platform.
     * Uses currently displayed metrics.
     */
    private fun retryBackgroundSync() {
        if (currentState.isBackgroundSyncing) return

        launch(
            onError = { /* handled in syncToBackendInBackground */ }
        ) {
            val userId = getCurrentUserId() ?: return@launch
            val metrics = currentState.healthMetrics

            if (metrics.isNotEmpty()) {
                updateState { copy(isBackgroundSyncing = true, backgroundSyncError = null) }
                syncToBackendInBackground(userId, metrics)
            }
        }
    }
}
