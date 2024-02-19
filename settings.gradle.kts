pluginManagement {
    includeBuild("gradle/plugins")
}
plugins {
    id("com.hedera.gradlebuild.settings")
}

include("sdk")
include("tck")
include("examples")
include("example-android")
