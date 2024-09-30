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

import org.springframework.boot.gradle.plugin.ResolveMainClassName
import org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES

plugins {
    id("org.springframework.boot") version "3.2.3"
    id("java")
    id("com.autonomousapps.dependency-analysis")
    id("com.hedera.gradle.base.lifecycle")
    id("com.hedera.gradle.check.javac-lint")
    id("com.hedera.gradle.check.spotless")
    id("com.hedera.gradle.check.spotless-java")
    id("com.hedera.gradle.check.spotless-kotlin")
    id("com.hedera.gradle.feature.git-properties-file")
    id("com.hedera.gradle.feature.java-compile")
    id("com.hedera.gradle.feature.java-doc")
    id("com.hedera.gradle.feature.java-execute")
    id("com.hedera.gradle.feature.test")
    id("com.hedera.gradle.report.test-logger")
}

description = "Hedera SDK TCK Server"

version = "0.0.1"

dependencies {
    annotationProcessor(platform(BOM_COORDINATES))
    annotationProcessor("org.projectlombok:lombok")

    implementation(platform(BOM_COORDINATES))
    implementation(platform(project(":hedera-dependency-versions")))
    implementation(project(":sdk"))
    implementation("com.thetransactioncompany:jsonrpc2-base")
    implementation("com.thetransactioncompany:jsonrpc2-server:2.0")
    implementation("net.minidev:json-smart")
    implementation("org.apache.tomcat.embed:tomcat-embed-core")
    implementation("org.bouncycastle:bcprov-jdk18on")
    implementation("org.slf4j:slf4j-api")
    implementation("org.springframework.boot:spring-boot")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-web")
    implementation("org.springframework:spring-webmvc")
    runtimeOnly("io.grpc:grpc-netty-shaded")
    runtimeOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.projectlombok:lombok")

    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito:mockito-junit-jupiter")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.withType<ResolveMainClassName> { setGroup(null) }

// Configure dependency analysis without using Java Modules
tasks.qualityGate { dependsOn(tasks.named("projectHealth")) }

tasks.qualityCheck { dependsOn(tasks.named("projectHealth")) }

dependencyAnalysis {
    issues {
        onAny {
            severity("fail")
            exclude("com.google.protobuf:protobuf-javalite")
        }
    }
}
