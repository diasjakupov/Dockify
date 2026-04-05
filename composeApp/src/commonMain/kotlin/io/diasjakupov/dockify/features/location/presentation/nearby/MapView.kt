package io.diasjakupov.dockify.features.location.presentation.nearby

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.diasjakupov.dockify.features.location.domain.model.Hospital
import io.diasjakupov.dockify.features.location.domain.model.Location
import io.diasjakupov.dockify.features.location.domain.model.NearbyUser

@Composable
expect fun MapView(
    userLocation: Location?,
    nearbyUsers: List<NearbyUser>,
    nearbyHospitals: List<Hospital>,
    onHospitalClick: (Hospital) -> Unit,
    darkTheme: Boolean,
    modifier: Modifier = Modifier
)
