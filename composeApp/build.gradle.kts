/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    // alias(libs.plugins.composeHotReload) TODO: Not working with current Kotlin version

    alias(libs.plugins.android.hilt)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.realm.kotlin)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.oss.licenses)
}

kotlin {
    explicitApi()

    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.uuid.ExperimentalUuidApi")
    }

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm("desktop") {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
            testLogging {
                showExceptions = true
                showStandardStreams = true
                events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
                exceptionFormat = TestExceptionFormat.FULL
            }
        }
    }

    sourceSets {
        val desktopMain by getting
        val desktopTest by getting

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            implementation(libs.android.core.ktx)
            implementation(libs.android.core.splashscreen)
            implementation(libs.google.accompanist.permissions)
            implementation(libs.android.workmanager)
            implementation(libs.google.material)

            // Data and storage
            implementation(libs.android.datastore.preferences)

            // Dependency Injection
            implementation(libs.dagger.hilt)
            implementation(libs.android.hilt.workmanager)
            implementation(libs.android.hilt.navigation.compose)

            // Adaptive
            implementation(libs.android.compose.adaptive)
            implementation(libs.android.compose.adaptive.layout)
            implementation(libs.android.compose.adaptive.navigation)

            // Navigation
            implementation(libs.android.navigation.compose)

            // Media player
            implementation(libs.telephoto)
            implementation(libs.google.accompanist.drawablepainter)
            implementation(libs.bundles.coil)

            // Cryptography
            implementation(libs.argon2.android)
            implementation(libs.android.biometric.ktx)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(compose.materialIconsExtended)
            implementation(libs.tink.android)
            implementation(libs.realm.kotlin)
            implementation(libs.kotlin.serialization.json)
            implementation(libs.logcat)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)

            implementation(libs.argon2.jvm)
        }
        desktopTest.dependencies {
            implementation(libs.junit.jupiter)
            runtimeOnly(libs.junit.platform.launcher)
        }
    }
}

dependencies {
    debugImplementation(compose.uiTooling)

    add("kspAndroid", libs.dagger.hilt.compiler)
    add("kspAndroid", libs.android.hilt.compiler)
}

// TODO: Compose Compiler metrics: no output?
composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    metricsDestination = layout.buildDirectory.dir("compose_compiler")
}

android {
    namespace = "de.lukaspieper.truvark"
    compileSdk = 35

    defaultConfig {
        minSdk = 29
        targetSdk = 35
        versionCode = 16
        versionName = "1.1.0"

        ndk {
            // Tink does not support 32-bit architectures (https://developers.google.com/tink/faq/support_for_32bit)
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-DEV"
            resValue("string", "app_name", "Truvark DEV")

            // Can be enabled for testing
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        release {
            resValue("string", "app_name", "Truvark")

            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }

    buildFeatures {
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        sarifReport = true
        abortOnError = false
        checkDependencies = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

android.applicationVariants.configureEach {
    val variantName = name

    val copyLicensesTask = tasks.register<Copy>("${variantName}CopyLicenses") {
        from("$rootDir/LICENSES")
        // TODO: Don't depend on oss-licenses-plugin's directory. Haven't figured `registerGeneratedResFolders` out yet.
        into(layout.buildDirectory.dir("generated/third_party_licenses/$variantName/res/raw"))

        rename { fileName ->
            fileName.lowercase()
                .removeSuffix(".txt")
                .replace("-", "_")
                .replace(".", "_")
        }
    }

    tasks.named("preBuild") {
        dependsOn(copyLicensesTask)
    }
}

compose.desktop {
    application {
        mainClass = "de.lukaspieper.truvark.AppKt"

        nativeDistributions {
            targetFormats(TargetFormat.Rpm)
            packageName = "Truvark"
            packageVersion = "0.1.0"
        }
    }
}
