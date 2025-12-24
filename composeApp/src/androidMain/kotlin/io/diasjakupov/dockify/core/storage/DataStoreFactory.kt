package io.diasjakupov.dockify.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

actual class DataStoreFactory(private val context: Context) {
    actual fun createPreferencesDataStore(): DataStore<Preferences> {
        return PreferenceDataStoreFactory.createWithPath(
            produceFile = {
                context.filesDir.resolve("dockify_prefs.preferences_pb").absolutePath.toPath()
            }
        )
    }
}
