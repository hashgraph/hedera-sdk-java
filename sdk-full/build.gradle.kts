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
    id("com.hedera.gradle.module.library")
    id("com.hedera.gradle.feature.protobuf")
}

description = "Hedera™ Hashgraph SDK for Java"

// Define dependency constraints for gRPC implementations so that clients automatically get the
// correct version
dependencies.constraints {
    api("io.grpc:grpc-netty:1.64.0")
    api("io.grpc:grpc-netty-shaded:1.64.0")
    api("io.grpc:grpc-okhttp:1.64.0")
}

javaModuleDependencies.moduleNameToGA.put(
    "com.google.protobuf",
    "com.google.protobuf:protobuf-java"
)

tasks.withType<JavaCompile>().configureEach { options.compilerArgs.add("-Xlint:-exports,-dep-ann") }

val sdkSrcMainProto = layout.projectDirectory.dir("../sdk/src/main/proto")
val sdkSrcMainJava =
    layout.projectDirectory.dir("../sdk/src/main/java").asFileTree.matching {
        exclude("module-info.java")
    }
val sdkSrcMainResources = layout.projectDirectory.dir("../sdk/src/main/resources")

tasks.generateProto {
    addIncludeDir(files(sdkSrcMainProto))
    addSourceDirs(files(sdkSrcMainProto))
}

tasks.compileJava { source(sdkSrcMainJava) }

tasks.processResources { from(sdkSrcMainResources) }

tasks.javadoc { source(sdkSrcMainJava) }

tasks.named<Jar>("sourcesJar") {
    from(sdkSrcMainJava)
    from(sdkSrcMainResources)
}

// 'sdk-full' is an alternative to 'sdk'. They cannot be used together.
// We express this via capability.
listOf(configurations.apiElements.get(), configurations.runtimeElements.get()).forEach {
    it.outgoing.capability(
        "${project.group}:${project.name}:${project.version}"
    ) // The default capability
    it.outgoing.capability("${project.group}:sdk:${project.version}") // The 'sdk' capability
}
