package io.diasjakupov.dockify.features.auth.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.core.storage.AuthPreferenceKeys
import io.diasjakupov.dockify.features.auth.data.dto.LoginResponseDto
import io.diasjakupov.dockify.features.auth.data.dto.UserResponseDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AuthLocalDataSourceImpl(
    private val dataStore: DataStore<Preferences>
) : AuthLocalDataSource {

    override suspend fun saveUser(user: LoginResponseDto): EmptyResult<DataError> {
        return try {
            val user = user.user
            dataStore.edit { preferences ->
                preferences[AuthPreferenceKeys.USER_ID] = user.id.toString()
                preferences[AuthPreferenceKeys.EMAIL] = user.email
                preferences[AuthPreferenceKeys.USERNAME] = user.username
                preferences[AuthPreferenceKeys.FIRST_NAME] = user.firstName
                preferences[AuthPreferenceKeys.LAST_NAME] = user.lastName
                preferences[AuthPreferenceKeys.CREATED_AT] = user.createdAt
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(DataError.Local.WRITE_ERROR)
        }
    }

    override suspend fun getUser(): Resource<UserResponseDto, DataError> {
        return try {
            val preferences = dataStore.data.first()
            val userId = preferences[AuthPreferenceKeys.USER_ID]

            if (userId == null) {
                Resource.Error(DataError.Local.NOT_FOUND)
            } else {
                val user = UserResponseDto(
                    id = userId.toInt(),
                    email = preferences[AuthPreferenceKeys.EMAIL] ?: "",
                    username = preferences[AuthPreferenceKeys.USERNAME] ?: "",
                    firstName = preferences[AuthPreferenceKeys.FIRST_NAME] ?: "",
                    lastName = preferences[AuthPreferenceKeys.LAST_NAME] ?: "",
                    createdAt = preferences[AuthPreferenceKeys.CREATED_AT] ?: ""
                )
                Resource.Success(user)
            }
        } catch (e: Exception) {
            Resource.Error(DataError.Local.READ_ERROR)
        }
    }

    override suspend fun clearUser(): EmptyResult<DataError> {
        return try {
            dataStore.edit { preferences ->
                preferences.clear()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(DataError.Local.WRITE_ERROR)
        }
    }

    override fun observeAuthState(): Flow<Boolean> {
        return dataStore.data
            .map { preferences ->
                preferences[AuthPreferenceKeys.USER_ID] != null
            }
            .catch { emit(false) }
    }

    override suspend fun hasUser(): Boolean {
        return try {
            val preferences = dataStore.data.first()
            preferences[AuthPreferenceKeys.USER_ID] != null
        } catch (e: Exception) {
            false
        }
    }
}
