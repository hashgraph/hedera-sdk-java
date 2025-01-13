// SPDX-License-Identifier: Apache-2.0
plugins {
    id("org.hiero.gradle.base.lifecycle")
    id("org.hiero.gradle.base.jpms-modules")
    id("org.hiero.gradle.check.spotless")
    id("org.hiero.gradle.check.spotless-kotlin")
}

group = "com.hedera.hashgraph"

val bouncycastle = "1.79"
val grpc = "1.69.0"
val protobuf = "4.29.3"
val slf4j = "2.0.16"

dependencies.constraints {
    api("com.esaulpaugh:headlong:12.3.3") { because("com.esaulpaugh.headlong") }
    api("com.google.code.findbugs:jsr305:3.0.2") { because("java.annotation") }
    api("com.google.code.gson:gson:2.11.0") { because("com.google.gson") }
    api("com.google.protobuf:protobuf-java:$protobuf") { because("com.google.protobuf") }
    api("com.google.protobuf:protobuf-javalite:$protobuf") { because("com.google.protobuf") }
    api("io.grpc:grpc-api:$grpc") { because("io.grpc") }
    api("io.grpc:grpc-inprocess:$grpc") { because("io.grpc.inprocess") }
    api("io.grpc:grpc-protobuf-lite:$grpc") { because("io.grpc.protobuf.lite") }
    api("io.grpc:grpc-protobuf:$grpc") { because("io.grpc.protobuf") }
    api("io.grpc:grpc-stub:$grpc") { because("io.grpc.stub") }
    api("io.grpc:grpc-netty:$grpc")
    api("io.grpc:grpc-netty-shaded:$grpc")
    api("io.grpc:grpc-okhttp:$grpc")
    api("org.bouncycastle:bcpkix-jdk18on:$bouncycastle") { because("org.bouncycastle.pkix") }
    api("org.bouncycastle:bcprov-jdk18on:$bouncycastle") { because("org.bouncycastle.provider") }
    api("org.slf4j:slf4j-api:$slf4j") { because("org.slf4j") }
    api("org.slf4j:slf4j-simple:$slf4j") { because("org.slf4j.simple") }

    // Testing
    api("com.fasterxml.jackson.core:jackson-core:2.18.2") { because("com.fasterxml.jackson.core") }
    api("com.google.guava:guava:33.4.0-android") { because("com.google.common") }
    api("io.github.cdimascio:java-dotenv:5.3.1") { because("java.dotenv") }
    api("io.github.json-snapshot:json-snapshot:1.0.17") { because("json.snapshot") }
    api("org.apache.commons:commons-lang3:3.17.0") { because("org.apache.commons.lang3") }
    api("org.assertj:assertj-core:3.27.2") { because("org.assertj.core") }
    api("org.junit.jupiter:junit-jupiter-api:5.11.4") { because("org.junit.jupiter.api") }
    api("org.mockito:mockito-core:5.15.2") { because("org.mockito") }

    api("com.google.protobuf:protoc:$protobuf")
    api("io.grpc:protoc-gen-grpc-java:$grpc")

    // Examples
    api("org.jetbrains.kotlin:kotlin-stdlib:2.1.0") { because("kotlin.stdlib") }
}
