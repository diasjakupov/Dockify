package io.diasjakupov.dockify.core.di

import io.diasjakupov.dockify.core.storage.DataStoreFactory
import org.koin.core.module.Module
import org.koin.dsl.module

val iosCoreModule: Module = module {
    single {
        DataStoreFactory()
    }

    single {
        get<DataStoreFactory>().createPreferencesDataStore()
    }
}
