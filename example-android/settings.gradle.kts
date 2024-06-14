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
