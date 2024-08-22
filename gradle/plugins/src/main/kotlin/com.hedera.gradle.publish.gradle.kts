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
    id("java")
    id("maven-publish")
    id("signing")
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<Jar>().configureEach { setGroup(null) }

val mavenJava = publishing.publications.create<MavenPublication>("mavenJava") {
    from(components["java"])

    versionMapping {
        allVariants { fromResolutionResult() }
    }

    suppressAllPomMetadataWarnings()

    pom {
        name = "Hedera SDK"
        description = "Hedera™ Hashgraph SDK for Java"
        url = "https://github.com/hashgraph/hedera-sdk-java"

        organization {
            name = "Hedera Hashgraph"
            url = "https://www.hedera.com"
        }

        issueManagement {
            system = "GitHub"
            url = "https://github.com/hashgraph/hedera-sdk-java/issues"
        }

        licenses {
            license {
                name = "Apache License, Version 2.0"
                url = "https://github.com/hashgraph/hedera-sdk-java/blob/main/LICENSE"
                distribution = "repo"
            }
        }

        scm {
            url = "https://github.com/hashgraph/hedera-sdk-java"
            connection = "scm:git:https://github.com/hashgraph/hedera-sdk-java.git"
            developerConnection = "scm:git:ssh://github.com:hashgraph/hedera-sdk-java.git"
        }

        developers {
            developer {
                name = "Nikita Lebedev"
            }

            developer {
                name = "Ivan Asenov"
            }
        }
    }
}

signing {
    sign(mavenJava)
    useGpgCmd()
}
