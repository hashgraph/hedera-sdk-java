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

import com.google.protobuf.gradle.id
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL

plugins {
    id("java-library")
    id("com.google.protobuf")
    id("com.hedera.gradle.java-base")
    id("com.hedera.gradle.publish")
}

@Suppress("UnstableApiUsage")
testing.suites {
    named<JvmTestSuite>("test") {
        useJUnitJupiter()
    }
    register<JvmTestSuite>("testIntegration") {
        testType = TestSuiteType.INTEGRATION_TEST
        targets.all {
            testTask {
                group = "build"
                systemProperty("CONFIG_FILE", providers.gradleProperty("CONFIG_FILE").getOrElse(""))
                systemProperty("HEDERA_NETWORK", providers.gradleProperty("HEDERA_NETWORK").getOrElse(""))
                systemProperty("OPERATOR_ID", providers.gradleProperty("OPERATOR_ID").getOrElse(""))
                systemProperty("OPERATOR_KEY", providers.gradleProperty("OPERATOR_KEY").getOrElse(""))
            }
        }
    }
}

tasks.withType<Test>().configureEach {
    // NOTE: Uncomment to enable trace logs in the SDK during tests
    // jvmArgs("-Dorg.slf4j.simpleLogger.log.com.hedera.hashgraph=trace")

    // emit logs per passed or failed test
    testLogging {
        exceptionFormat = FULL
        events("passed", "skipped", "failed", "standardOut", "standardError")
    }

    // propagate system environment to test runner
    systemProperty("OPERATOR_ID", providers.gradleProperty("OPERATOR_ID").getOrElse(""))
    systemProperty("OPERATOR_KEY", providers.gradleProperty("OPERATOR_KEY").getOrElse(""))
    systemProperty("CONFIG_FILE", providers.gradleProperty("CONFIG_FILE").getOrElse(""))
    systemProperty("HEDERA_NETWORK", providers.gradleProperty("HEDERA_NETWORK").getOrElse(""))
}

tasks.withType<Javadoc>().configureEach {
    options {
        this as StandardJavadocDocletOptions
        encoding = "UTF-8"
        addStringOption("Xdoclint:all,-missing", "-quiet")
        addStringOption("Xwerror", "-quiet")
    }
}

tasks.jacocoTestReport {
    // make sure to use any/all test coverage data for the report and run all tests before this report is made
    executionData.from(
        tasks.test.map { it.extensions.getByType<JacocoTaskExtension>().destinationFile!! },
        tasks.named("testIntegration").map { it.extensions.getByType<JacocoTaskExtension>().destinationFile!! }
    )

    // remove generated proto files from report
    classDirectories.setFrom(sourceSets.main.get().output.asFileTree.matching {
        exclude(
            "**/proto/**",
            "**/AccountAllowanceAdjustTransaction.*",
            "**/HederaPreCheckStatusException.*",
            "**/HederaReceiptStatusException.*"
        )
    })

    // configure it so only xml is generated for the report
    reports {
        xml.required = true
        html.required = true
        csv.required = false
    }
}


// https://github.com/google/protobuf-gradle-plugin
protobuf {
    protoc {
        // shouldn't be updated for now (breaking changes after 4.x.x)
        artifact = "com.google.protobuf:protoc:3.25.4"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.66.0"
        }
    }
}
tasks.generateProto {
    plugins { plugins.register("grpc") { option("@generated=omit") } }
}

tasks.compileJava {
    options.javaModuleVersion = project.version.toString()
}

tasks.jar {
    exclude("**/*.proto")
    includeEmptyDirs = false
    manifest {
        attributes["Implementation-Version"] = project.version
    }
}

sourceSets.all {
    configurations[getTaskName("", "compileProtoPath")].extendsFrom(configurations["internal"])
}
