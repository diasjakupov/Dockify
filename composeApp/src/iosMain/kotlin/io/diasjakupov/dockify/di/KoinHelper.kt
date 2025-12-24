package io.diasjakupov.dockify.di

import io.diasjakupov.dockify.core.di.iosCoreModule
import io.diasjakupov.dockify.features.health.di.iosHealthModule
import io.diasjakupov.dockify.features.location.di.iosLocationModule
import org.koin.core.context.startKoin

/**
 * Helper object for initializing Koin on iOS.
 * This should be called from the iOS app delegate.
 */
fun initKoin() {
    startKoin {
        modules(
            appModules() + listOf(iosCoreModule, iosHealthModule, iosLocationModule)
        )
    }
}
