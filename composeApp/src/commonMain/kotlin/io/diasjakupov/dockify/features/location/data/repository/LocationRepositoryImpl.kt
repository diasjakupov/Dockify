package io.diasjakupov.dockify.features.location.data.repository

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.location.data.datasource.LocationPlatformDataSource
import io.diasjakupov.dockify.features.location.data.datasource.LocationRemoteDataSource
import io.diasjakupov.dockify.features.location.data.mapper.LocationMapper.createNearestHospitalsRequest
import io.diasjakupov.dockify.features.location.data.mapper.LocationMapper.createNearestUsersRequest
import io.diasjakupov.dockify.features.location.data.mapper.LocationMapper.toDomainHospitals
import io.diasjakupov.dockify.features.location.data.mapper.LocationMapper.toDomainUsers
import io.diasjakupov.dockify.features.location.domain.model.Hospital
import io.diasjakupov.dockify.features.location.domain.model.Location
import io.diasjakupov.dockify.features.location.domain.model.NearbyUser
import io.diasjakupov.dockify.features.location.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of LocationRepository that coordinates between
 * platform-specific and remote data sources.
 */
class LocationRepositoryImpl(
    private val platformDataSource: LocationPlatformDataSource,
    private val remoteDataSource: LocationRemoteDataSource
) : LocationRepository {

    override suspend fun getCurrentLocation(): Resource<Location, DataError> {
        return platformDataSource.getCurrentLocation()
    }

    override suspend fun getNearestUsers(
        location: Location,
        radiusMeters: Double,
        currentUserId: String
    ): Resource<List<NearbyUser>, DataError> {
        val request = createNearestUsersRequest(location, radiusMeters, currentUserId)
        return when (val result = remoteDataSource.getNearestUsers(request)) {
            is Resource.Success -> Resource.Success(result.data.toDomainUsers())
            is Resource.Error -> result
        }
    }

    override suspend fun getNearestHospitals(
        location: Location,
        radiusMeters: Double
    ): Resource<List<Hospital>, DataError> {
        val request = createNearestHospitalsRequest(location, radiusMeters)
        return when (val result = remoteDataSource.getNearestHospitals(request)) {
            is Resource.Success -> Resource.Success(result.data.toDomainHospitals())
            is Resource.Error -> result
        }
    }

    override fun observeLocation(): Flow<Resource<Location, DataError>> {
        return platformDataSource.observeLocation()
    }

    override suspend fun hasLocationPermission(): Boolean {
        return platformDataSource.hasPermission()
    }

    override suspend fun requestLocationPermission(): EmptyResult<DataError> {
        return platformDataSource.requestPermission()
    }

    override suspend fun isLocationEnabled(): Boolean {
        return platformDataSource.isLocationEnabled()
    }
}
