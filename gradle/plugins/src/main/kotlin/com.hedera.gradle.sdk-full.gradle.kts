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
    id("com.hedera.gradle.java")
}

javaModuleDependencies.moduleNameToGA.put("com.google.protobuf", "com.google.protobuf:protobuf-java")

val sdkSrcMainProto = layout.projectDirectory.dir("../sdk/src/main/proto")
val sdkSrcMainJava = layout.projectDirectory.dir("../sdk/src/main/java").asFileTree.matching {
    exclude("module-info.java")
}
val sdkSrcMainResources = layout.projectDirectory.dir("../sdk/src/main/resources")

tasks.generateProto {
    addIncludeDir(files(sdkSrcMainProto))
    addSourceDirs(files(sdkSrcMainProto))
}

tasks.compileJava {
    source(sdkSrcMainJava)
}

tasks.processResources {
    from(sdkSrcMainResources)
}

tasks.javadoc {
    source(sdkSrcMainJava)
}

tasks.named<Jar>("sourcesJar") {
    from(sdkSrcMainJava)
    from(sdkSrcMainResources)
}

// 'sdk-full' is an alternative to 'sdk'. They cannot be used together.
// We express this via capability.
val sdkCapability = "${project.group}:sdk:${project.version}"
val sdkFullCapability = "${project.group}:${project.name}:${project.version}"
configurations.apiElements {
    outgoing.capability(sdkFullCapability) // The default capability
    outgoing.capability(sdkCapability) // The 'sdk' capability
}
configurations.runtimeElements {
    outgoing.capability(sdkFullCapability) // The default capability
    outgoing.capability(sdkCapability) // The 'sdk' capability
}
