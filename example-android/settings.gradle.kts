// SPDX-License-Identifier: Apache-2.0

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

// --- Remove to use a published SDK version ---
includeBuild("..")
// ---------------------------------------------

rootProject.name = "Android Example"

include("app")
