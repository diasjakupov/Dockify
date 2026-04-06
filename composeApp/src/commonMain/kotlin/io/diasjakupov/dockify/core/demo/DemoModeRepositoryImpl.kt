package io.diasjakupov.dockify.core.demo

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import io.diasjakupov.dockify.core.storage.AppPreferenceKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class DemoModeRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : DemoModeRepository {

    override fun isDemoMode(): Flow<Boolean> {
        return dataStore.data
            .map { preferences -> preferences[AppPreferenceKeys.DEMO_MODE_ENABLED] ?: false }
            .catch { emit(false) }
    }

    override suspend fun setDemoMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AppPreferenceKeys.DEMO_MODE_ENABLED] = enabled
        }
    }
}
