plugins {
    id("com.hedera.gradlebuild.sdk.java")
}

extraJavaModuleInfo {
    module("com.google.protobuf:protobuf-javalite", "com.google.protobuf.UNUSED")
    module("com.google.protobuf:protobuf-java", "com.google.protobuf") {
        exportAllPackages()
        requireAllDefinedDependencies()
        requires("java.logging")
        requires("jdk.unsupported")
    }
}

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

tasks.sourcesJar {
    from(sdkSrcMainJava)
    from(sdkSrcMainResources)
}

// 'sdk-full' is an alternative to 'sdk'. They cannot be used together.
// We express this via capability.
val sdkCapability = "${project.group}:sdk:${project.version}"
configurations.apiElements {
    outgoing.capability(sdkCapability)
}
configurations.runtimeElements {
    outgoing.capability(sdkCapability)
}
