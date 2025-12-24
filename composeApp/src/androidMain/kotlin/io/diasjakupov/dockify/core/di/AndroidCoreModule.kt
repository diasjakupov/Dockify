package io.diasjakupov.dockify.core.di

import io.diasjakupov.dockify.core.storage.DataStoreFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

val androidCoreModule: Module = module {
    single {
        DataStoreFactory(androidContext())
    }

    single {
        get<DataStoreFactory>().createPreferencesDataStore()
    }
}
