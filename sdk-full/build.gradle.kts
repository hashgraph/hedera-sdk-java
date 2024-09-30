// SPDX-License-Identifier: Apache-2.0
plugins {
    id("org.hiero.gradle.module.library")
    id("org.hiero.gradle.feature.protobuf")
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
