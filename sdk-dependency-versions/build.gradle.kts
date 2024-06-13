/*
 * Copyright (C) 2023 Hedera Hashgraph, LLC
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
    id("com.hedera.gradle.versions")
}

dependencies.constraints {
    api("com.esaulpaugh:headlong:10.0.0") {
        because("headlong")
    }
    api("com.github.spotbugs:spotbugs-annotations:4.7.3") {
        because("com.github.spotbugs.annotations")
    }
    api("com.google.guava:guava:32.1.3-jre") {
        because("com.google.common")
    }
    api("com.google.errorprone:error_prone_annotations:2.21.1") {
        because("com.google.errorprone.annotations")
    }
    api("com.google.code.gson:gson:2.10.1") {
        because("com.google.gson")
    }
    api("com.google.protobuf:protobuf-java:3.21.9") {
        because("com.google.protobuf")
    }
    api("com.google.protobuf:protobuf-javalite:3.21.9") {
        because("com.google.protobuf")
    }
    api("io.grpc:grpc-api:1.64.0") {
        because("io.grpc.stub")
    }
    api("io.grpc:grpc-inprocess:1.64.0") {
        because("io.grpc.protobuf")
    }
    api("io.grpc:grpc-protobuf:1.64.0") {
        because("io.grpc.protobuf")
    }
    api("io.grpc:grpc-protobuf-lite:1.64.0") {
        because("io.grpc.protobuf")
    }
    api("io.grpc:grpc-stub:1.64.0") {
        because("io.grpc.stub")
    }
    api("javax.annotation:javax.annotation-api:1.3.2") {
        because("java.annotation")
    }
    api("org.bouncycastle:bcpkix-jdk18on:1.76") {
        because("org.bouncycastle.pkix")
    }
    api("org.bouncycastle:bcprov-jdk18on:1.76") {
        because("org.bouncycastle.provider")
    }
    api("org.slf4j:slf4j-api:2.0.9") {
        because("org.slf4j")
    }
    api("org.slf4j:slf4j-simple:2.0.9") {
        because("org.slf4j.simple")
    }

    // Testing
    api("io.grpc:grpc-netty-shaded:1.64.0") {
        because("io.grpc.netty.shaded")
    }
    api("io.github.cdimascio:java-dotenv:5.3.1") {
        because("java.dotenv")
    }
    api("io.github.json-snapshot:json-snapshot:1.0.17") {
        because("json.snapshot")
    }
    api("org.apache.commons:commons-lang3:3.14.0") {
        because("org.apache.commons.lang3")
    }
    api("org.assertj:assertj-core:3.24.2") {
        because("org.assertj.core")
    }

    // Examples
    api("org.jetbrains.kotlin:kotlin-stdlib:1.9.22") {
        because("kotlin.stdlib")
    }
}
