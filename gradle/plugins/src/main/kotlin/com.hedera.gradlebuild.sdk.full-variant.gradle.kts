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

plugins {
    id("java")
    id("com.google.protobuf")
    id("com.hedera.gradlebuild.patch-modules")
    id("com.hedera.gradlebuild.sdk.publish")
}

val publishFull = providers.gradleProperty("full").getOrElse("false").toBoolean()

protobuf.generateProtoTasks {
    all().configureEach {
        builtins.named("java") {
            if (!publishFull) option("lite")
        }
        plugins.register("grpc") {
            if (!publishFull) option("lite")
        }
    }
}

if (publishFull) {
    extraJavaModuleInfo {
        module("com.google.protobuf:protobuf-javalite", "com.google.protobuf.UNUSED")
        module("com.google.protobuf:protobuf-java", "com.google.protobuf") {
            exportAllPackages()
            requireAllDefinedDependencies()
            requires("java.logging")
            requires("jdk.unsupported")
        }
    }
}

publishing.publications.named<MavenPublication>("mavenJava") {
    if (publishFull) artifactId = "sdk-full"
}
