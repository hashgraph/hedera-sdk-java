pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

includeBuild("..")

rootProject.name = "Android Example"

include("app")
