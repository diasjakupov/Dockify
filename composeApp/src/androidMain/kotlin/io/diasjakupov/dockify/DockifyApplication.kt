package io.diasjakupov.dockify

import android.app.Application
import io.diasjakupov.dockify.core.di.androidCoreModule
import io.diasjakupov.dockify.di.appModules
import io.diasjakupov.dockify.features.health.di.androidHealthModule
import io.diasjakupov.dockify.features.location.di.androidLocationModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Application class for Dockify Android app.
 * Initializes Koin dependency injection.
 */
class DockifyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@DockifyApplication)
            modules(
                appModules() + listOf(androidCoreModule, androidHealthModule, androidLocationModule)
            )
        }
    }
}
