package io.diasjakupov.dockify.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey

expect class DataStoreFactory {
    fun createPreferencesDataStore(): DataStore<Preferences>
}

object AuthPreferenceKeys {
    val USER_ID = stringPreferencesKey("user_id")
    val EMAIL = stringPreferencesKey("email")
    val USERNAME = stringPreferencesKey("username")
    val FIRST_NAME = stringPreferencesKey("first_name")
    val LAST_NAME = stringPreferencesKey("last_name")
    val CREATED_AT = stringPreferencesKey("created_at")
}
