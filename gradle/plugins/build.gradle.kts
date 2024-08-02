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
    `kotlin-dsl`
}

repositories.gradlePluginPortal()

dependencies {
    implementation("com.autonomousapps:dependency-analysis-gradle-plugin:1.33.0")
    implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:6.0.12")
    implementation("com.google.protobuf:protobuf-gradle-plugin:0.9.4")
    implementation("io.github.gradle-nexus:publish-plugin:1.3.0")
    implementation("net.ltgt.gradle:gradle-errorprone-plugin:3.1.0")
    implementation("org.gradlex:extra-java-module-info:1.8")
    implementation("org.gradlex:java-module-dependencies:1.6.5")
    implementation("org.gradlex:jvm-dependency-conflict-resolution:2.1")
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:4.4.1.3373")

    implementation("org.gradle.toolchains:foojay-resolver:0.8.0")
    implementation("com.gradle:develocity-gradle-plugin:3.17.2")
}
