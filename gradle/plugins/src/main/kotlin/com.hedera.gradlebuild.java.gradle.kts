import net.ltgt.gradle.errorprone.errorprone

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
    id("jacoco")
    id("net.ltgt.errorprone")
    id("org.gradlex.java-module-dependencies")
    id("org.gradlex.java-module-versions")
    id("com.hedera.gradlebuild.base")
    id("com.hedera.gradlebuild.repositories")
    id("com.hedera.gradlebuild.patch-modules")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
        vendor = JvmVendorSpec.ADOPTIUM
    }
}

jacoco {
    toolVersion = "0.8.8"
}

javaModuleDependencies {
    versionsFromPlatformAndConsistentResolution(":sdk", ":sdk")
}

dependencies {
    // https://github.com/google/error-prone
    // https://errorprone.info/
    errorprone("com.google.errorprone:error_prone_core:2.21.1")

    // https://github.com/uber/NullAway
    errorprone("com.uber.nullaway:nullaway:0.10.14")

    // https://github.com/grpc/grpc-java-api-checker
    errorprone("io.grpc:grpc-java-api-checker:1.1.0")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"

    // Disable warnings because I'm tired of them :P
    options.isWarnings = false

    options.errorprone {
        // https://github.com/uber/NullAway
        warn("NullAway")
        option("NullAway:AnnotatedPackages", "com.hedera.hashgraph.sdk")
        option("NullAway:TreatGeneratedAsUnannotated", "true")

        // https://github.com/grpc/grpc-java-api-checker
        disable("GrpcExperimentalApi")
        warn("GrpcInternal")

        // Enable _all_ error prone checks then selectively disble
        // Checks that are default-disabled are enabled as warnings
        allDisabledChecksAsWarnings = true
        disable("TryFailRefactoring")
        disable("ThrowSpecificExceptions")
        disable("FutureReturnValueIgnored")
        disable("FieldCanBeFinal")
        disable("Finally")
        disable("BooleanParameter")
        disable("ThreadJoinLoop")
        disable("UnnecessaryDefaultInEnumSwitch")
        disable("UngroupedOverloads")
        disable("InlineMeSuggester")

        // Uncomment do disable Android + JDK7 checks
        // disable("Java7ApiChecker")
        // disable("AndroidJdkLibsChecker")

        // Ignore generated and protobuf code
        disableWarningsInGeneratedCode = true
        excludedPaths = ".*generated.*"
    }
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
    fileMode = 436 // octal: 0664
    dirMode = 509 // octal: 0775
}
