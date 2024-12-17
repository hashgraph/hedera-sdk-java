// SPDX-License-Identifier: Apache-2.0
plugins {
    id("org.hiero.gradle.module.library")
    id("org.hiero.gradle.feature.protobuf")
}

description = "Hedera™ Hashgraph SDK for Java"

// TODO following block to be extracted into a plugin
//      https://github.com/hiero-ledger/hiero-gradle-conventions/issues/41
val publishDependencyConstraint =
    configurations.create("publishDependencyConstraint") {
        extendsFrom(configurations.internal.get())
        dependencies.all {
            val addedDependency = this
            project.dependencies.constraints.add(
                "api",
                incoming.resolutionResult.rootComponent.map {
                    (it.dependencies.single {
                            it is ResolvedDependencyResult &&
                                it.selected.moduleVersion?.group == addedDependency.group &&
                                it.selected.moduleVersion?.name == addedDependency.name
                        } as ResolvedDependencyResult)
                        .selected
                        .moduleVersion
                        .toString()
                }
            )
        }
    }

// Define dependency constraints for gRPC implementations so that clients automatically get the
// correct version
dependencies {
    publishDependencyConstraint("io.grpc:grpc-netty")
    publishDependencyConstraint("io.grpc:grpc-netty-shaded")
    publishDependencyConstraint("io.grpc:grpc-okhttp")
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
