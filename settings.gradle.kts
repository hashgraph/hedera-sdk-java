/*
 * Copyright (C) 2024 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

pluginManagement {
    repositories.gradlePluginPortal()
    repositories.maven("https://repo.onepiece.software/snapshots")
}

plugins { id("com.hedera.gradle.build") version "0.0.2" }

rootProject.name = "hedera-sdk-java"

javaModules {
    module("sdk") { group = "com.hedera.hashgraph" }
    module("sdk-full") { group = "com.hedera.hashgraph" }
    module("tck") { group = "com.hedera.hashgraph.sdk.tck" }
}

includeBuild("examples")

includeBuild("example-android")
