package io.diasjakupov.dockify.features.location.presentation.nearby

import io.diasjakupov.dockify.core.demo.DemoModeRepository
import io.diasjakupov.dockify.core.demo.FakeDataProvider
import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.auth.domain.usecase.GetCurrentUserUseCase
import io.diasjakupov.dockify.features.location.domain.model.Hospital
import io.diasjakupov.dockify.features.location.domain.model.NearbyUser
import io.diasjakupov.dockify.features.location.domain.repository.LocationRepository
import io.diasjakupov.dockify.features.location.domain.usecase.GetCurrentLocationUseCase
import io.diasjakupov.dockify.features.location.domain.usecase.GetNearestHospitalsUseCase
import io.diasjakupov.dockify.features.location.domain.usecase.GetNearestUsersUseCase
import io.diasjakupov.dockify.features.location.permission.LocationPermissionHandler
import io.diasjakupov.dockify.ui.base.BaseViewModel
import io.diasjakupov.dockify.ui.base.LoadingState
import io.diasjakupov.dockify.ui.base.toUserMessage

class NearbyViewModel(
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val getNearestUsersUseCase: GetNearestUsersUseCase,
    private val getNearestHospitalsUseCase: GetNearestHospitalsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val locationRepository: LocationRepository,
    private val permissionHandler: LocationPermissionHandler,
    private val demoModeRepository: DemoModeRepository
) : BaseViewModel<NearbyState, NearbyAction, NearbyEffect>(NearbyState()) {

    companion object {
        private const val DEFAULT_RADIUS_METERS = 5000.0
    }

    private var isDemoMode = false
    private var realUsers: List<NearbyUser> = emptyList()
    private var realHospitals: List<Hospital> = emptyList()

    init {
        onAction(NearbyAction.CheckPermissionAndLoadData)
        observeDemoMode()
    }

    private fun observeDemoMode() {
        collectFlow(demoModeRepository.isDemoMode()) { newDemoMode ->
            this@NearbyViewModel.isDemoMode = newDemoMode
            copy(
                nearbyUsers = if (newDemoMode) realUsers + FakeDataProvider.nearbyUsers else realUsers,
                nearbyHospitals = if (newDemoMode) realHospitals + FakeDataProvider.nearbyHospitals else realHospitals
            )
        }
    }

    override fun handleAction(action: NearbyAction) {
        when (action) {
            is NearbyAction.CheckPermissionAndLoadData -> checkPermissionAndLoadData()
            is NearbyAction.RequestPermission -> requestPermission()
            is NearbyAction.PermissionGranted -> handlePermissionGranted()
            is NearbyAction.PermissionDenied -> handlePermissionDenied()
            is NearbyAction.RefreshNearbyUsers -> refreshData()
            is NearbyAction.DismissError -> updateState { copy(error = null) }
            is NearbyAction.OpenLocationSettings -> emitEffect(NearbyEffect.OpenGpsSettings)
            is NearbyAction.RetryLastAction -> retryLastAction()
            is NearbyAction.SelectTab -> updateState { copy(selectedTab = action.tab) }
            is NearbyAction.OpenDirections -> handleOpenDirections(action.hospital)
        }
    }

    private fun handleOpenDirections(hospital: Hospital) {
        val name = hospital.name ?: "Hospital"
        emitEffect(
            NearbyEffect.OpenDirections(
                latitude = hospital.location.latitude,
                longitude = hospital.location.longitude,
                label = name
            )
        )
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

            val hasPermission = permissionHandler.hasLocationPermission()

            if (hasPermission) {
                updateState { copy(permissionState = LocationPermissionState.Granted) }
                loadData()
            } else {
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
            val granted = permissionHandler.requestLocationPermission()

            if (granted) {
                updateState { copy(permissionState = LocationPermissionState.Granted) }
                loadData()
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
            val granted = permissionHandler.requestLocationPermission()

            if (granted) {
                updateState { copy(permissionState = LocationPermissionState.Granted) }
                loadData()
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

    private fun loadData() {
        launch(
            onError = { e ->
                updateState {
                    copy(
                        loadingState = LoadingState.IDLE,
                        error = e.message ?: "Failed to load data",
                        hasInitiallyLoaded = true
                    )
                }
            }
        ) {
            val userId = getCurrentUserId() ?: return@launch

            when (val locationResult = getCurrentLocationUseCase()) {
                is Resource.Success -> {
                    val location = locationResult.data
                    updateState { copy(currentLocation = location) }
                    emitEffect(NearbyEffect.LocationFetched)

                    // Fetch nearby users
                    when (val usersResult = getNearestUsersUseCase(
                        location = location,
                        radiusMeters = DEFAULT_RADIUS_METERS,
                        currentUserId = userId
                    )) {
                        is Resource.Success -> {
                            realUsers = usersResult.data
                            val displayUsers = if (isDemoMode) realUsers + FakeDataProvider.nearbyUsers else realUsers
                            updateState { copy(nearbyUsers = displayUsers) }
                        }
                        is Resource.Error -> {
                            updateState { copy(error = usersResult.error.toUserMessage()) }
                        }
                    }

                    // Fetch nearby hospitals (non-blocking — errors don't prevent users from showing)
                    when (val hospitalsResult = getNearestHospitalsUseCase(
                        location = location,
                        radiusMeters = DEFAULT_RADIUS_METERS
                    )) {
                        is Resource.Success -> {
                            realHospitals = hospitalsResult.data
                            val displayHospitals = if (isDemoMode) realHospitals + FakeDataProvider.nearbyHospitals else realHospitals
                            updateState { copy(nearbyHospitals = displayHospitals) }
                        }
                        is Resource.Error -> { /* non-blocking */ }
                    }

                    updateState {
                        copy(
                            loadingState = LoadingState.IDLE,
                            hasInitiallyLoaded = true,
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    handleLocationError(locationResult.error)
                }
            }
        }
    }

    private fun refreshData() {
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
                            realUsers = usersResult.data
                            val displayUsers = if (isDemoMode) realUsers + FakeDataProvider.nearbyUsers else realUsers
                            updateState { copy(nearbyUsers = displayUsers) }
                        }
                        is Resource.Error -> {
                            updateState { copy(error = usersResult.error.toUserMessage()) }
                        }
                    }

                    when (val hospitalsResult = getNearestHospitalsUseCase(
                        location = location,
                        radiusMeters = DEFAULT_RADIUS_METERS
                    )) {
                        is Resource.Success -> {
                            realHospitals = hospitalsResult.data
                            val displayHospitals = if (isDemoMode) realHospitals + FakeDataProvider.nearbyHospitals else realHospitals
                            updateState { copy(nearbyHospitals = displayHospitals) }
                        }
                        is Resource.Error -> { /* non-blocking */ }
                    }

                    updateState {
                        copy(
                            isManualRefreshing = false,
                            error = null
                        )
                    }
                    emitEffect(NearbyEffect.ShowSnackbar("Location updated"))
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
