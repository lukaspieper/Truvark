/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false

    alias(libs.plugins.google.ksp) apply false
    alias(libs.plugins.android.hilt) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.realm.kotlin) apply false
    alias(libs.plugins.google.oss.licenses) apply false

    alias(libs.plugins.detekt)
}

dependencies {
    detektPlugins(libs.detekt.rules.formatting)
    detektPlugins(libs.detekt.rules.compose)
}

tasks.detekt {
    config.from(file(".detekt/detekt.yml"))
    baseline = file(".detekt/detekt-baseline.xml")
    buildUponDefaultConfig = true
    parallel = true
    setSource(files("composeApp/src"))
}

tasks.detektBaseline {
    config.from(file(".detekt/detekt.yml"))
    baseline = file(".detekt/detekt-baseline.xml")
    buildUponDefaultConfig = true
    parallel = true
    setSource(files("composeApp/src"))
}
