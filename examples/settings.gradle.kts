pluginManagement {
    includeBuild("../gradle/plugins")
    repositories {
        gradlePluginPortal()
    }
}

// --- Remove to use a published SDK version ---
includeBuild("..")
// ---------------------------------------------
