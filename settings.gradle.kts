pluginManagement {
    includeBuild("gradle/plugins")
}
plugins {
    id("com.hedera.gradlebuild.settings")
}

includeBuild("example-android")

include("examples")

include("sdk")
include("sdk-full")
