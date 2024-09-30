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
    id("com.hedera.gradle.versions")
}

dependencies.constraints {
    api("com.esaulpaugh:headlong:12.1.0") {
        because("headlong")
    }
    api("com.google.code.gson:gson:2.10.1") {
        because("com.google.gson")
    }
    api("com.google.protobuf:protobuf-java:3.25.3") {
        // shouldn't be updated for now (breaking changes after 4.x.x)
        because("com.google.protobuf")
    }
    api("com.google.protobuf:protobuf-javalite:3.25.3") {
        // shouldn't be updated for now (breaking changes after 4.x.x)
        because("com.google.protobuf")
    }
    api("io.grpc:grpc-api:1.64.0") {
        because("io.grpc")
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
    api("com.google.code.findbugs:jsr305:3.0.2") {
        because("java.annotation")
    }
    api("org.bouncycastle:bcpkix-jdk18on:1.78.1") {
        because("org.bouncycastle.pkix")
    }
    api("org.bouncycastle:bcprov-jdk18on:1.78.1") {
        because("org.bouncycastle.provider")
    }
    api("org.slf4j:slf4j-api:2.0.9") {
        because("org.slf4j")
    }
    api("org.slf4j:slf4j-simple:2.0.16") {
        because("org.slf4j.simple")
    }

    // Testing
    api("io.github.cdimascio:java-dotenv:5.3.1") {
        because("java.dotenv")
    }
    api("io.github.json-snapshot:json-snapshot:1.0.17") {
        because("json.snapshot")
    }
    api("org.apache.commons:commons-lang3:3.17.0") {
        because("org.apache.commons.lang3")
    }
    api("org.assertj:assertj-core:3.26.3") {
        because("org.assertj.core")
    }

    // Examples
    api("org.jetbrains.kotlin:kotlin-stdlib:2.0.20") {
        because("kotlin.stdlib")
    }
}
