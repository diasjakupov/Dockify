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
import io.diasjakupov.dockify.core.demo.DemoModeRepository
import io.diasjakupov.dockify.core.demo.FakeDataProvider
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
    private val locationPermissionHandler: LocationPermissionHandler,
    private val demoModeRepository: DemoModeRepository
) : BaseViewModel<HealthState, HealthAction, HealthEffect>(HealthState()) {

    private val allMetricTypes = HealthMetricType.entries.toList()
    private var isDemoMode = false
    private var realMetrics: List<HealthMetric> = emptyList()

    init {
        onAction(HealthAction.CheckPermissionsAndAutoSync)
        observeDemoMode()
    }

    private fun observeDemoMode() {
        collectFlow(demoModeRepository.isDemoMode()) { newDemoMode ->
            this@HealthViewModel.isDemoMode = newDemoMode
            val displayMetrics = if (newDemoMode) {
                mergeWithFakeMetrics(realMetrics)
            } else {
                realMetrics
            }
            copy(healthMetrics = displayMetrics)
        }
    }

    private fun mergeWithFakeMetrics(real: List<HealthMetric>): List<HealthMetric> {
        val realTypes = real.map { it.type }.toSet()
        val fakeToAdd = FakeDataProvider.healthMetrics.filter { it.type !in realTypes }
        return real + fakeToAdd
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

        println("[HealthViewModel] syncHealthData: starting manual sync")
        updateState { copy(isSyncing = true) }

        launch(
            onError = { e ->
                println("[HealthViewModel] syncHealthData: exception - ${e.message}")
                updateState {
                    copy(
                        isSyncing = false,
                        error = e.message ?: "Sync failed"
                    )
                }
            }
        ) {
            val userId = getCurrentUserId() ?: run {
                println("[HealthViewModel] syncHealthData: no userId, aborting")
                updateState { copy(isSyncing = false) }
                return@launch
            }

            val location = getLocationSilently()
            println("[HealthViewModel] syncHealthData: userId=$userId location=$location")

            when (val result = syncHealthDataUseCase(userId, allMetricTypes, location)) {
                is Resource.Success -> {
                    println("[HealthViewModel] syncHealthData: success")
                    updateState {
                        copy(
                            isSyncing = false,
                            lastSyncTimestamp = Clock.System.now().toEpochMilliseconds()
                        )
                    }
                    emitEffect(HealthEffect.SyncSuccess)
                    emitEffect(HealthEffect.ShowSnackbar("Health data synced successfully"))
                }
                is Resource.Error -> {
                    val errorMessage = result.error.toUserMessage()
                    println("[HealthViewModel] syncHealthData: error - ${result.error} -> $errorMessage")
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
        launch {
            val userId = getCurrentUserId() ?: return@launch
            println("[HealthViewModel] loadRecommendation: userId=$userId")
            when (val result = getRecommendationUseCase(userId)) {
                is Resource.Success -> {
                    println("[HealthViewModel] loadRecommendation: success - ${result.data.content.take(60)}")
                    updateState { copy(recommendation = result.data, isRecommendationLoading = false) }
                }
                is Resource.Error -> {
                    println("[HealthViewModel] loadRecommendation: error - ${result.error}")
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
            println("[HealthViewModel] checkPermissionsAndAutoSync: skipped (already loaded)")
            return
        }

        println("[HealthViewModel] checkPermissionsAndAutoSync: starting")
        updateState { copy(loadingState = LoadingState.LOADING) }

        launch(
            onError = { e ->
                println("[HealthViewModel] checkPermissionsAndAutoSync: exception - ${e.message}")
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
            println("[HealthViewModel] platform available: $isAvailable")
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
                return@launch
            }

            // Step 2: Check permissions
            val hasPermissions = checkHealthPermissionsUseCase.hasPermissions(allMetricTypes)
            println("[HealthViewModel] hasPermissions: $hasPermissions")

            if (hasPermissions) {
                updateState { copy(permissionState = PermissionState.Granted) }
                requestLocationPermissionSilently()
                autoSyncWithOptimisticUpdate(userId)
            } else {
                updateState {
                    copy(
                        permissionState = PermissionState.Unknown,
                        loadingState = LoadingState.IDLE
                    )
                }
                requestPermissionsAndThenSync(userId)
            }
        }
    }

    /**
     * Requests health permissions and then syncs data after permission is granted.
     * Called on initial screen load when permissions are not yet granted.
     */
    private fun requestPermissionsAndThenSync(userId: String) {
        println("[HealthViewModel] requestPermissionsAndThenSync: requesting permissions for userId=$userId")
        launch(
            onError = { e ->
                println("[HealthViewModel] requestPermissionsAndThenSync: exception - ${e.message}")
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
            println("[HealthViewModel] requestPermissionsAndThenSync: granted=$granted")

            if (granted) {
                updateState { copy(permissionState = PermissionState.Granted) }
                // Request location permission (non-blocking, in background)
                requestLocationPermissionSilently()
                // Permissions granted - proceed with auto-sync
                autoSyncWithOptimisticUpdate(userId)
            } else {
                println("[HealthViewModel] requestPermissionsAndThenSync: permission denied by user")
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
                println("[HealthViewModel] autoSyncWithOptimisticUpdate: exception - ${e.message}")
                updateState {
                    copy(
                        loadingState = LoadingState.IDLE,
                        error = e.message ?: "Failed to read health data",
                        hasInitiallyLoadedPlatformData = true
                    )
                }
            }
        ) {
            println("[HealthViewModel] reading platform health data (${allMetricTypes.size} types)")
            when (val platformResult = readPlatformHealthDataUseCase(allMetricTypes)) {
                is Resource.Success -> {
                    val platformMetrics = platformResult.data
                    println("[HealthViewModel] platform read success: ${platformMetrics.size} metrics")
                    platformMetrics.forEach { m ->
                        println("[HealthViewModel]   ${m.type.name} = ${m.value} ${m.unit}")
                    }

                    realMetrics = platformMetrics
                    updateState {
                        copy(
                            healthMetrics = if (isDemoMode) mergeWithFakeMetrics(platformMetrics) else platformMetrics,
                            loadingState = LoadingState.IDLE,
                            hasInitiallyLoadedPlatformData = true,
                            isBackgroundSyncing = true,
                            backgroundSyncError = null
                        )
                    }

                    loadRecommendation()
                    syncToBackendInBackground(userId, platformMetrics)
                }
                is Resource.Error -> {
                    println("[HealthViewModel] platform read error: ${platformResult.error}")
                    updateState {
                        copy(
                            loadingState = LoadingState.IDLE,
                            error = platformResult.error.toUserMessage(),
                            hasInitiallyLoadedPlatformData = true
                        )
                    }
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
                println("[HealthViewModel] syncToBackendInBackground: exception - ${e.message}")
                updateState {
                    copy(
                        isBackgroundSyncing = false,
                        backgroundSyncError = e.message ?: "Failed to sync to server"
                    )
                }
                emitEffect(HealthEffect.BackgroundSyncFailed)
            }
        ) {
            println("[HealthViewModel] syncToBackendInBackground: userId=$userId metrics=${metrics.size}")
            val location = getLocationSilently()
            println("[HealthViewModel] syncToBackendInBackground: location=$location")

            val healthData = HealthData(
                userId = userId,
                metrics = metrics,
                location = location
            )

            when (val result = healthRepository.syncHealthData(healthData)) {
                is Resource.Success -> {
                    println("[HealthViewModel] syncToBackendInBackground: success")
                    updateState {
                        copy(
                            isBackgroundSyncing = false,
                            backgroundSyncError = null,
                            lastSyncTimestamp = Clock.System.now().toEpochMilliseconds()
                        )
                    }
                }
                is Resource.Error -> {
                    val errorMessage = result.error.toUserMessage()
                    println("[HealthViewModel] syncToBackendInBackground: error - ${result.error} -> $errorMessage")
                    updateState {
                        copy(
                            isBackgroundSyncing = false,
                            backgroundSyncError = errorMessage
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

        println("[HealthViewModel] retryBackgroundSync: starting")
        launch(
            onError = { /* handled in syncToBackendInBackground */ }
        ) {
            val userId = getCurrentUserId() ?: return@launch
            val metrics = currentState.healthMetrics

            if (metrics.isNotEmpty()) {
                println("[HealthViewModel] retryBackgroundSync: userId=$userId metrics=${metrics.size}")
                updateState { copy(isBackgroundSyncing = true, backgroundSyncError = null) }
                syncToBackendInBackground(userId, metrics)
            } else {
                println("[HealthViewModel] retryBackgroundSync: no metrics to sync, skipping")
            }
        }
    }
}
