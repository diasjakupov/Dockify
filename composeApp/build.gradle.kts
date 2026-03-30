import java.util.Properties

import com.google.firebase.appdistribution.gradle.firebaseAppDistribution
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
    id("com.google.gms.google-services")
    id("com.google.firebase.appdistribution")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.health.connect)
            implementation(libs.play.services.location)
            implementation(libs.maps.compose)
            implementation(libs.koin.android)
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)

            // Inspektify (Network Inspector)
            implementation(libs.inspektify.ktor3)

            // Kotlinx
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)

            // DataStore
            implementation(libs.datastore.preferences.core)

            // FileKit — cross-platform file picker
            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs.compose)

            // Navigation 3
            implementation(libs.navigation3.ui)
            implementation(libs.lifecycle.viewmodel.navigation3)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "io.diasjakupov.dockify"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "io.diasjakupov.dockify"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }
        val mapsApiKey = localProperties.getProperty("MAPS_API_KEY") ?: ""
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("debug") {
            firebaseAppDistribution {
                artifactType = "APK"
                groups = "testing"
                releaseNotesFile = "${rootProject.projectDir}/release-notes.txt"
            }
        }
        getByName("release") {
            isMinifyEnabled = false
            firebaseAppDistribution {
                artifactType = "APK"
                groups = "testing"
                releaseNotesFile = "${rootProject.projectDir}/release-notes.txt"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

abstract class GenerateReleaseNotesTask : DefaultTask() {
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Input
    abstract val projectDirPath: Property<String>

    @TaskAction
    fun generate() {
        val process = ProcessBuilder("git", "log", "--oneline", "-10")
            .directory(File(projectDirPath.get()))
            .redirectErrorStream(true)
            .start()
        val gitLog = process.inputStream.bufferedReader().readText().trim()
        process.waitFor()

        val file = outputFile.get().asFile
        file.writeText("Recent changes:\n$gitLog")
        logger.lifecycle("Release notes written to ${file.path}")
    }
}

val generateReleaseNotes by tasks.registering(GenerateReleaseNotesTask::class) {
    group = "distribution"
    description = "Generates release notes from recent git commits"
    outputFile.set(rootProject.layout.projectDirectory.file("release-notes.txt"))
    projectDirPath.set(rootProject.projectDir.absolutePath)
}

afterEvaluate {
    tasks.named("appDistributionUploadDebug") {
        dependsOn(generateReleaseNotes)
    }
}

tasks.register("publishToAppDistribution") {
    group = "distribution"
    description = "Assembles debug APK with git-based release notes and uploads to Firebase App Distribution"

    dependsOn("assembleDebug")

    doLast {
        // appDistributionUploadDebug is triggered via finalizedBy after evaluation
    }
}

afterEvaluate {
    tasks.named("publishToAppDistribution") {
        dependsOn("appDistributionUploadDebug")
    }
}

