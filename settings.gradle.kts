pluginManagement {
    includeBuild("gradle/plugins")
}
plugins {
    id("com.hedera.gradlebuild.settings")
}

includeBuild("example-android")

include("sdk")
include("examples")
