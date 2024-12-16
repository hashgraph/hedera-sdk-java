// SPDX-License-Identifier: Apache-2.0
import org.springframework.boot.gradle.plugin.ResolveMainClassName
import org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES

plugins {
    id("org.springframework.boot") version "3.2.3"
    id("java")
    id("com.autonomousapps.dependency-analysis")
    id("org.hiero.gradle.base.lifecycle")
    id("org.hiero.gradle.check.javac-lint")
    id("org.hiero.gradle.check.spotless")
    id("org.hiero.gradle.check.spotless-java")
    id("org.hiero.gradle.check.spotless-kotlin")
    id("org.hiero.gradle.feature.git-properties-file")
    id("org.hiero.gradle.feature.java-compile")
    id("org.hiero.gradle.feature.java-doc")
    id("org.hiero.gradle.feature.java-execute")
    id("org.hiero.gradle.feature.test")
    id("org.hiero.gradle.report.test-logger")
}

description = "Hiero SDK TCK Server"

version = "0.0.1"

dependencies {
    annotationProcessor(platform(BOM_COORDINATES))
    annotationProcessor("org.projectlombok:lombok")

    implementation(platform(BOM_COORDINATES))
    implementation(platform(project(":hiero-dependency-versions")))
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
            onUnusedDependencies { exclude(":sdk") }
        }
    }
}
