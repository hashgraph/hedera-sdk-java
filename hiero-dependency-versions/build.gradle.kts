// SPDX-License-Identifier: Apache-2.0
plugins {
    id("org.hiero.gradle.base.lifecycle")
    id("org.hiero.gradle.base.jpms-modules")
    id("org.hiero.gradle.check.spotless")
    id("org.hiero.gradle.check.spotless-kotlin")
}

group = "com.hedera.hashgraph"

dependencies.constraints {
    api("com.esaulpaugh:headlong:12.3.3") { because("com.esaulpaugh.headlong") }
    api("com.google.code.gson:gson:2.11.0") { because("com.google.gson") }
    api("com.google.protobuf:protobuf-java:4.29.1") { because("com.google.protobuf") }
    api("com.google.protobuf:protobuf-javalite:4.29.1") { because("com.google.protobuf") }
    api("io.grpc:grpc-api:1.69.0") { because("io.grpc") }
    api("io.grpc:grpc-inprocess:1.67.1") { because("io.grpc.inprocess") }
    api("io.grpc:grpc-protobuf:1.69.0") { because("io.grpc.protobuf") }
    api("io.grpc:grpc-protobuf-lite:1.69.0") { because("io.grpc.protobuf.lite") }
    api("io.grpc:grpc-stub:1.68.2") { because("io.grpc.stub") }
    api("com.google.code.findbugs:jsr305:3.0.2") { because("java.annotation") }
    api("org.bouncycastle:bcpkix-jdk18on:1.79") { because("org.bouncycastle.pkix") }
    api("org.bouncycastle:bcprov-jdk18on:1.79") { because("org.bouncycastle.provider") }
    api("org.slf4j:slf4j-api:2.0.9") { because("org.slf4j") }
    api("org.slf4j:slf4j-simple:2.0.16") { because("org.slf4j.simple") }
    api("com.google.code.findbugs:jsr305:3.0.2") { because("javax.annotation") }

    // Testing
    api("com.fasterxml.jackson.core:jackson-core:2.18.2") { because("com.fasterxml.jackson.core") }
    api("io.github.cdimascio:java-dotenv:5.3.1") { because("java.dotenv") }
    api("io.github.json-snapshot:json-snapshot:1.0.17") { because("json.snapshot") }
    api("org.apache.commons:commons-lang3:3.17.0") { because("org.apache.commons.lang3") }
    api("org.assertj:assertj-core:3.26.3") { because("org.assertj.core") }
    api("org.junit.jupiter:junit-jupiter-api:5.11.3") { because("org.junit.jupiter.api") }
    api("org.mockito:mockito-core:5.14.2") { because("org.mockito") }
    api("com.google.guava:guava:33.3.1-android") { because("com.google.common") }
    api("com.fasterxml.jackson.core:jackson-core:2.18.2") { because("com.fasterxml.jackson.core") }

    api("com.google.protobuf:protoc:4.29.1")
    api("io.grpc:protoc-gen-grpc-java:1.69.0")

    // Examples
    api("org.jetbrains.kotlin:kotlin-stdlib:2.1.0") { because("kotlin.stdlib") }
}
