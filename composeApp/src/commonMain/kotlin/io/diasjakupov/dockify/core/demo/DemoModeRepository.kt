package io.diasjakupov.dockify.core.demo

import kotlinx.coroutines.flow.Flow

interface DemoModeRepository {
    fun isDemoMode(): Flow<Boolean>
    suspend fun setDemoMode(enabled: Boolean)
}
