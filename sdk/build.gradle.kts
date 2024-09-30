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
    id("com.hedera.gradle.feature.test-integration")
}

description = "Hedera™ Hashgraph SDK for Java"

javaModuleDependencies.moduleNameToGA.put(
    "com.google.protobuf",
    "com.google.protobuf:protobuf-javalite"
)

// Define dependency constraints for gRPC implementations so that clients automatically get the
// correct version
dependencies.constraints {
    api("io.grpc:grpc-netty:1.64.0")
    api("io.grpc:grpc-netty-shaded:1.64.0")
    api("io.grpc:grpc-okhttp:1.64.0")
}

testModuleInfo {
    requires("com.fasterxml.jackson.annotation")
    requires("com.fasterxml.jackson.core")
    requires("com.fasterxml.jackson.databind")
    requires("json.snapshot")
    requires("org.assertj.core")
    requires("org.junit.jupiter.api")
    requires("org.junit.jupiter.params")
    requires("org.mockito")

    requiresStatic("java.annotation")

    runtimeOnly("io.grpc.netty.shaded")
    runtimeOnly("org.slf4j.simple")
}

testIntegrationModuleInfo {
    runtimeOnly("io.grpc.netty.shaded")
    runtimeOnly("org.slf4j.simple")
}

protobuf {
    generateProtoTasks {
        all().configureEach {
            builtins.named("java") { option("lite") }
            plugins.named("grpc") { option("lite") }
        }
    }
}

tasks.withType<Test>().configureEach {
    systemProperty("CONFIG_FILE", providers.gradleProperty("CONFIG_FILE").getOrElse(""))
    systemProperty("HEDERA_NETWORK", providers.gradleProperty("HEDERA_NETWORK").getOrElse(""))
    systemProperty("OPERATOR_ID", providers.gradleProperty("OPERATOR_ID").getOrElse(""))
    systemProperty("OPERATOR_KEY", providers.gradleProperty("OPERATOR_KEY").getOrElse(""))
}

tasks.withType<JavaCompile>().configureEach { options.compilerArgs.add("-Xlint:-exports,-dep-ann") }

dependencyAnalysis.abi {
    exclusions {
        // Exposes: org.slf4j.Logger
        excludeClasses("logger")
        // Exposes: com.google.common.base.MoreObjects.ToStringHelper
        excludeClasses(".*\\.CustomFee")
        // Exposes: com.esaulpaugh.headlong.abi.Tuple
        excludeClasses(".*\\.ContractFunctionResult")
        // Exposes: org.bouncycastle.crypto.params.KeyParameter
        excludeClasses(".*\\.PrivateKey.*")
        // Exposes: io.grpc.stub.AbstractFutureStub (and others)
        excludeClasses(".*Grpc")
    }
}
