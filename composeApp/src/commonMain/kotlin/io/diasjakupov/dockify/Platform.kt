package io.diasjakupov.dockify

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform