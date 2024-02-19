pluginManagement {
    includeBuild("gradle/plugins")
}
plugins {
    id("com.hedera.gradlebuild.settings")
}

include("sdk")
include("examples")
include("example-android")
