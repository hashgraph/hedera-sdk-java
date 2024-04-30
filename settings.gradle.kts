pluginManagement {
    includeBuild("gradle/plugins")
}
plugins {
    id("com.hedera.gradle.settings")
}

includeBuild("example-android")

include("sdk")
include("sdk-full")
include("tck")
include("examples")
