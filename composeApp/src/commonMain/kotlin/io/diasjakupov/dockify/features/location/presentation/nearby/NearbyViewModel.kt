package io.diasjakupov.dockify.features.location.presentation.nearby

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.auth.domain.usecase.GetCurrentUserUseCase
import io.diasjakupov.dockify.features.location.domain.repository.LocationRepository
import io.diasjakupov.dockify.features.location.domain.usecase.GetCurrentLocationUseCase
import io.diasjakupov.dockify.features.location.domain.usecase.GetNearestUsersUseCase
import io.diasjakupov.dockify.features.location.permission.LocationPermissionHandler
import io.diasjakupov.dockify.ui.base.BaseViewModel
import io.diasjakupov.dockify.ui.base.LoadingState
import io.diasjakupov.dockify.ui.base.toUserMessage

/**
 * ViewModel for the Nearby screen.
 * Manages location fetching, permission handling, and nearby users display.
 */
class NearbyViewModel(
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val getNearestUsersUseCase: GetNearestUsersUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val locationRepository: LocationRepository,
    private val permissionHandler: LocationPermissionHandler
) : BaseViewModel<NearbyState, NearbyAction, NearbyEffect>(NearbyState()) {

    companion object {
        private const val DEFAULT_RADIUS_METERS = 5000.0 // 5km radius
    }

    init {
        onAction(NearbyAction.CheckPermissionAndLoadData)
    }

    override fun handleAction(action: NearbyAction) {
        when (action) {
            is NearbyAction.CheckPermissionAndLoadData -> checkPermissionAndLoadData()
            is NearbyAction.RequestPermission -> requestPermission()
            is NearbyAction.PermissionGranted -> handlePermissionGranted()
            is NearbyAction.PermissionDenied -> handlePermissionDenied()
            is NearbyAction.RefreshNearbyUsers -> refreshNearbyUsers()
            is NearbyAction.DismissError -> updateState { copy(error = null) }
            is NearbyAction.OpenLocationSettings -> emitEffect(NearbyEffect.OpenGpsSettings)
            is NearbyAction.RetryLastAction -> retryLastAction()
        }
    }

    private fun checkPermissionAndLoadData() {
        if (currentState.hasInitiallyLoaded) {
            return
        }

        updateState { copy(loadingState = LoadingState.LOADING) }

        launch(
            onError = { e ->
                updateState {
                    copy(
                        loadingState = LoadingState.IDLE,
                        error = e.message ?: "Failed to check permissions",
                        hasInitiallyLoaded = true
                    )
                }
            }
        ) {
            // Check if GPS is enabled
            val isLocationEnabled = locationRepository.isLocationEnabled()
            if (!isLocationEnabled) {
                updateState {
                    copy(
                        permissionState = LocationPermissionState.GpsDisabled,
                        loadingState = LoadingState.IDLE,
                        hasInitiallyLoaded = true,
                        error = "Location services are disabled. Please enable GPS."
                    )
                }
                return@launch
            }

            // Check permissions using the handler
            val hasPermission = permissionHandler.hasLocationPermission()

            if (hasPermission) {
                updateState { copy(permissionState = LocationPermissionState.Granted) }
                loadNearbyUsers()
            } else {
                // Request permissions - this will show the system dialog
                requestPermissionAndThenLoad()
            }
        }
    }

    private fun requestPermissionAndThenLoad() {
        launch(
            onError = { e ->
                updateState {
                    copy(
                        permissionState = LocationPermissionState.Denied,
                        loadingState = LoadingState.IDLE,
                        hasInitiallyLoaded = true,
                        error = e.message ?: "Failed to request permissions"
                    )
                }
            }
        ) {
            // Request permission - this will show the system dialog and suspend until user responds
            val granted = permissionHandler.requestLocationPermission()

            if (granted) {
                updateState { copy(permissionState = LocationPermissionState.Granted) }
                loadNearbyUsers()
            } else {
                updateState {
                    copy(
                        permissionState = LocationPermissionState.Denied,
                        loadingState = LoadingState.IDLE,
                        hasInitiallyLoaded = true
                    )
                }
            }
        }
    }

    private fun requestPermission() {
        updateState { copy(loadingState = LoadingState.LOADING) }

        launch(
            onError = { e ->
                updateState {
                    copy(
                        permissionState = LocationPermissionState.Denied,
                        loadingState = LoadingState.IDLE,
                        error = e.message
                    )
                }
            }
        ) {
            // Request permission - this will show the system dialog and suspend until user responds
            val granted = permissionHandler.requestLocationPermission()

            if (granted) {
                updateState { copy(permissionState = LocationPermissionState.Granted) }
                loadNearbyUsers()
            } else {
                updateState {
                    copy(
                        permissionState = LocationPermissionState.Denied,
                        loadingState = LoadingState.IDLE
                    )
                }
                emitEffect(NearbyEffect.ShowSnackbar("Location permission denied"))
            }
        }
    }

    private fun handlePermissionGranted() {
        updateState {
            copy(
                permissionState = LocationPermissionState.Granted,
                hasInitiallyLoaded = false
            )
        }
        onAction(NearbyAction.CheckPermissionAndLoadData)
    }

    private fun handlePermissionDenied() {
        updateState { copy(permissionState = LocationPermissionState.Denied) }
        emitEffect(NearbyEffect.ShowSnackbar("Location permission denied"))
    }

    private fun loadNearbyUsers() {
        launch(
            onError = { e ->
                updateState {
                    copy(
                        loadingState = LoadingState.IDLE,
                        error = e.message ?: "Failed to load nearby users",
                        hasInitiallyLoaded = true
                    )
                }
            }
        ) {
            val userId = getCurrentUserId() ?: return@launch

            // Get current location
            when (val locationResult = getCurrentLocationUseCase()) {
                is Resource.Success -> {
                    val location = locationResult.data
                    updateState { copy(currentLocation = location) }
                    emitEffect(NearbyEffect.LocationFetched)

                    // Fetch nearby users from API
                    when (val usersResult = getNearestUsersUseCase(
                        location = location,
                        radiusMeters = DEFAULT_RADIUS_METERS,
                        currentUserId = userId
                    )) {
                        is Resource.Success -> {
                            updateState {
                                copy(
                                    nearbyUsers = usersResult.data,
                                    loadingState = LoadingState.IDLE,
                                    hasInitiallyLoaded = true,
                                    error = null
                                )
                            }
                        }
                        is Resource.Error -> {
                            updateState {
                                copy(
                                    loadingState = LoadingState.IDLE,
                                    hasInitiallyLoaded = true,
                                    error = usersResult.error.toUserMessage()
                                )
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    handleLocationError(locationResult.error)
                }
            }
        }
    }

    private fun refreshNearbyUsers() {
        if (!currentState.canRefresh) return

        updateState { copy(isManualRefreshing = true) }

        launch(
            onError = { e ->
                updateState {
                    copy(
                        isManualRefreshing = false,
                        error = e.message ?: "Failed to refresh"
                    )
                }
            }
        ) {
            val userId = getCurrentUserId() ?: run {
                updateState { copy(isManualRefreshing = false) }
                return@launch
            }

            when (val locationResult = getCurrentLocationUseCase()) {
                is Resource.Success -> {
                    val location = locationResult.data
                    updateState { copy(currentLocation = location) }

                    when (val usersResult = getNearestUsersUseCase(
                        location = location,
                        radiusMeters = DEFAULT_RADIUS_METERS,
                        currentUserId = userId
                    )) {
                        is Resource.Success -> {
                            updateState {
                                copy(
                                    nearbyUsers = usersResult.data,
                                    isManualRefreshing = false,
                                    error = null
                                )
                            }
                            emitEffect(NearbyEffect.ShowSnackbar("Location updated"))
                        }
                        is Resource.Error -> {
                            updateState {
                                copy(
                                    isManualRefreshing = false,
                                    error = usersResult.error.toUserMessage()
                                )
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    updateState { copy(isManualRefreshing = false) }
                    handleLocationError(locationResult.error)
                }
            }
        }
    }

    private fun handleLocationError(error: DataError) {
        when (error) {
            DataError.Location.PERMISSION_DENIED -> {
                updateState {
                    copy(
                        permissionState = LocationPermissionState.Denied,
                        loadingState = LoadingState.IDLE,
                        hasInitiallyLoaded = true
                    )
                }
            }
            DataError.Location.GPS_DISABLED -> {
                updateState {
                    copy(
                        permissionState = LocationPermissionState.GpsDisabled,
                        loadingState = LoadingState.IDLE,
                        hasInitiallyLoaded = true,
                        error = "Location services are disabled"
                    )
                }
            }
            else -> {
                updateState {
                    copy(
                        loadingState = LoadingState.IDLE,
                        hasInitiallyLoaded = true,
                        error = error.toUserMessage()
                    )
                }
            }
        }
    }

    private fun retryLastAction() {
        updateState { copy(hasInitiallyLoaded = false, error = null) }
        checkPermissionAndLoadData()
    }

    private suspend fun getCurrentUserId(): String? {
        return when (val result = getCurrentUserUseCase()) {
            is Resource.Success -> result.data.id
            is Resource.Error -> {
                updateState {
                    copy(
                        loadingState = LoadingState.IDLE,
                        error = "Please log in to view nearby users"
                    )
                }
                null
            }
        }
    }
}
