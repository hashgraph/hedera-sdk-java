pluginManagement {
    includeBuild("gradle/plugins")
}
plugins {
    id("com.hedera.gradle.settings")
}

includeBuild("example-android")

include("sdk")
include("sdk-full")
include("sdk-dependency-versions")
include("tck")
include("examples")
