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
    id("com.hedera.gradlebuild.repositories")
    id("com.autonomousapps.dependency-analysis")
    id("io.github.gradle-nexus.publish-plugin")
}

val productVersion = layout.projectDirectory.file("version.txt").asFile.readText().trim()

tasks.register("showVersion") {
    group = "versioning"

    inputs.property("version", productVersion)

    doLast { println(inputs.properties["version"]) }
}

nexusPublishing {
    repositories {
        sonatype { }
    }
}

tasks.named("closeSonatypeStagingRepository") {
    // The publishing of all components to Maven Central (in this case only 'sdk') is
    // automatically done before close (which is done before release).
    dependsOn(":sdk:publishToSonatype")
}
