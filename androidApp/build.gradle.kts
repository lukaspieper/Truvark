/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)

    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.koin.compiler)
    alias(libs.plugins.google.oss.licenses)
}

kotlin {
    explicitApi()

    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.uuid.ExperimentalUuidApi")
        freeCompilerArgs.add("-opt-in=kotlinx.serialization.ExperimentalSerializationApi")

        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(projects.shared)

    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)

    implementation(libs.android.core.splashscreen)
    implementation(libs.google.accompanist.permissions)
    implementation(libs.android.workmanager)
    implementation(libs.google.material)
    implementation(libs.androidx.navigation.compose)

    // Data and storage
    implementation(libs.android.datastore.preferences)

    // Dependency Injection
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.androidx.compose.navigation)
    implementation(libs.koin.androidx.workmanager)
    implementation(libs.koin.annotations)

    // Adaptive
    implementation(libs.android.compose.adaptive)
    implementation(libs.android.compose.adaptive.layout)
    implementation(libs.android.compose.adaptive.navigation)

    // Media
    implementation(libs.telephoto)
    implementation(libs.google.accompanist.drawablepainter)
    implementation(libs.bundles.coil)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui.compose)

    // Cryptography
    implementation(libs.argon2.android)
    implementation(libs.android.biometric.compose)
}

android {
    namespace = "de.lukaspieper.truvark"
    compileSdk = 36

    defaultConfig {
        minSdk = 31
        targetSdk = 36
        versionCode = 203
        versionName = "2.0.3"

        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

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
        }
        release {
            resValue("string", "app_name", "Truvark")

            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
        }
    }

    buildFeatures {
        resValues = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs.keepDebugSymbols.add("**/*.so") // F-Droid reproducible builds failed without it.
    }

    dependenciesInfo {
        // F-Droid apk check failed without them.
        includeInApk = false
        includeInBundle = false
    }

    lint {
        sarifReport = true
        abortOnError = false
        checkDependencies = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

androidComponents {
    onVariants { variant ->
        val copyLicensesTask = tasks.register<Copy>("${variant.name}CopyLicenses") {
            from("$rootDir/LICENSES")
            // TODO: Don't depend on oss-licenses-plugin's directory (it already changed in the past).
            into(layout.buildDirectory.dir("generated/res/${variant.name}OssLicensesTask/raw"))

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
}
