/*-
 *
 * Hedera Java SDK
 *
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
 *
 */

plugins {
    id("java")
    id("org.gradlex.extra-java-module-info")
    id("org.gradlex.jvm-dependency-conflict-resolution")
}

// Fix or enhance the metadata of third-party Modules. This is about the metadata in the
// repositories: '*.pom' and '*.module' files.
jvmDependencyConflicts.patch {
    // These compile time annotation libraries are not of interest in our setup and are thus removed
    // from the dependencies of all components that bring them in.
    val annotationLibraries =
        listOf(
            "com.google.android:annotations",
            "com.google.code.findbugs:annotations",
            "com.google.code.findbugs:jsr305",
            "com.google.guava:listenablefuture",
            "com.google.j2objc:j2objc-annotations",
            "org.checkerframework:checker-compat-qual",
            "org.checkerframework:checker-qual",
            "org.codehaus.mojo:animal-sniffer-annotations"
        )

    module("com.github.spotbugs:spotbugs-annotations") { annotationLibraries.forEach { removeDependency(it) } }
    module("com.google.guava:guava") { annotationLibraries.forEach { removeDependency(it) } }
    module("io.grpc:grpc-api") { annotationLibraries.forEach { removeDependency(it) } }
    module("io.grpc:grpc-context") { annotationLibraries.forEach { removeDependency(it) } }
    module("io.grpc:grpc-core") { annotationLibraries.forEach { removeDependency(it) } }
    module("io.grpc:grpc-netty-shaded") { annotationLibraries.forEach { removeDependency(it) } }
    module("io.grpc:grpc-protobuf") { annotationLibraries.forEach { removeDependency(it) } }
    module("io.grpc:grpc-protobuf-lite") { annotationLibraries.forEach { removeDependency(it) } }
    module("io.grpc:grpc-util") { annotationLibraries.forEach { removeDependency(it) } }

    // Testing only
    module("org.jetbrains.kotlin:kotlin-stdlib") { removeDependency("org.jetbrains.kotlin:kotlin-stdlib-common") }
    module("junit:junit") { removeDependency("org.hamcrest:hamcrest-core") }
}

// Fix or enhance the 'module-info.class' of third-party Modules. This is about the
// 'module-info.class' inside the Jar files. In our full Java Modules setup every
// Jar needs to have this file. If it is missing, it is added by what is configured here.
extraJavaModuleInfo {
    failOnAutomaticModules = true // Only allow Jars with 'module-info' on all module paths

    module("com.esaulpaugh:headlong", "headlong")
    module("com.github.spotbugs:spotbugs-annotations", "com.github.spotbugs.annotations")
    module("com.google.errorprone:error_prone_annotations", "com.google.errorprone.annotations")
    module("com.google.guava:failureaccess", "com.google.common.util.concurrent.internal")
    module("com.google.guava:guava", "com.google.common") {
        exportAllPackages()
        requireAllDefinedDependencies()
        requires("java.logging")
    }
    module("com.google.protobuf:protobuf-java", "com.google.protobuf") {
        exportAllPackages()
        requireAllDefinedDependencies()
        requires("java.logging")
        requires("jdk.unsupported")
    }
    module("com.google.protobuf:protobuf-javalite", "com.google.protobuf") {
        exportAllPackages()
        requireAllDefinedDependencies()
        requires("java.logging")
        requires("jdk.unsupported")
    }
    module("io.grpc:grpc-api", "io.grpc") {
        exportAllPackages()
        requireAllDefinedDependencies()
        requires("java.logging")
        uses("io.grpc.LoadBalancerProvider")
        uses("io.grpc.ManagedChannelProvider")
        uses("io.grpc.NameResolverProvider")
    }
    module("io.grpc:grpc-context", "io.grpc.context")
    module("io.grpc:grpc-core", "io.grpc.internal") {
        exportAllPackages()
        requireAllDefinedDependencies()
        requires("java.logging")
    }
    module("io.grpc:grpc-inprocess", "io.grpc.inprocess")
    module("io.grpc:grpc-protobuf-lite", "io.grpc.protobuf.lite")
    module("io.grpc:grpc-protobuf", "io.grpc.protobuf")
    module("io.grpc:grpc-stub", "io.grpc.stub") {
        exportAllPackages()
        requireAllDefinedDependencies()
        requires("java.logging")
    }
    module("io.perfmark:perfmark-api", "io.perfmark")
    module("com.google.code.findbugs:jsr305", "java.annotation")
    module("org.jetbrains:annotations", "org.jetbrains.annotations")

    // Full protobuf only
    module("com.google.api.grpc:proto-google-common-protos", "com.google.api.grpc.common")

    // Testing only
    module("com.fasterxml.jackson.core:jackson-annotations", "com.fasterxml.jackson.annotations")
    module("com.fasterxml.jackson.core:jackson-core", "com.fasterxml.jackson.core")
    module("com.fasterxml.jackson.core:jackson-databind", "com.fasterxml.jackson.databind") {
        exportAllPackages()
        requireAllDefinedDependencies()
        requires("java.sql")
    }
    module("io.grpc:grpc-netty-shaded", "io.grpc.netty.shaded") {
        exportAllPackages()
        requireAllDefinedDependencies()
        requires("java.logging")
        requires("jdk.unsupported")
        ignoreServiceProvider("reactor.blockhound.integration.BlockHoundIntegration")
    }
    module("io.grpc:grpc-util", "io.grpc.util")
    module("io.github.cdimascio:java-dotenv", "java.dotenv")
    module("io.github.json-snapshot:json-snapshot", "json.snapshot")
    module("junit:junit", "junit")
    module("org.mockito:mockito-core", "org.mockito")
    module("org.mockito:mockito-junit-jupiter", "org.mockito.junit.jupiter")
    module("org.objenesis:objenesis", "org.objenesis")
}
