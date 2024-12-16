// SPDX-License-Identifier: Apache-2.0
plugins { id("org.hiero.gradle.build") version "0.1.1" }

rootProject.name = "hiero-sdk-java"

javaModules {
    module("sdk") { group = "com.hiero" }
    module("sdk-full") { group = "com.hiero" }
    module("tck") { group = "com.hiero.sdk.tck" }
}

// includeBuild("examples")
//
// includeBuild("example-android")
