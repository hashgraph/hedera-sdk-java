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

import com.google.protobuf.gradle.id
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL

plugins {
    id("java-library")
    id("com.google.protobuf")
    id("com.github.spotbugs")
    id("org.sonarqube")
    id("com.hedera.gradlebuild.java-base")
    id("com.hedera.gradlebuild.publish")
}

@Suppress("UnstableApiUsage")
testing.suites {
    named<JvmTestSuite>("test") {
        useJUnitJupiter()
    }
    register<JvmTestSuite>("integrationTest") {
        testType = TestSuiteType.INTEGRATION_TEST
        targets.all { testTask { group = "build" } }
    }
}

tasks.withType<Test>().configureEach {
    // NOTE: Uncomment to enable trace logs in the SDK during tests
    // jvmArgs("-Dorg.slf4j.simpleLogger.log.com.hedera.hashgraph=trace")

    // this task will fail on the first failed test
    failFast = true

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
        tasks.named("integrationTest").map { it.extensions.getByType<JacocoTaskExtension>().destinationFile!! }
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
        artifact = "com.google.protobuf:protoc:3.24.3"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.58.0"
        }
    }
}
tasks.generateProto {
    plugins { plugins.register("grpc") }
}

tasks.jar {
    exclude("**/*.proto")
    includeEmptyDirs = false
}

sourceSets.all {
    configurations[getTaskName("", "compileProtoPath")].extendsFrom(configurations["internal"])
}

spotbugs {
    //ignoreFailures = false
    //showStackTraces = true
    //showProgress = false
    //reportLevel = 'default'
    //effort = 'default'
    //visitors = [ 'FindSqlInjection', 'SwitchFallthrough' ]
    //omitVisitors = [ 'FindNonShortCircuit' ]
    reportsDir = layout.buildDirectory.dir("reports/spotbugs")
    //includeFilter = file('spotbugs-include.xml')
    //excludeFilter = file('spotbugs-exclude.xml')
    onlyAnalyze = listOf("com.hedera.hashgraph.sdk.*")
    //projectName = name
    //release = version
    //extraArgs = [ '-nested:false' ]
    //jvmArgs = [ '-Duser.language=ja' ]
    //maxHeapSize = '512m'
}

tasks.spotbugsMain {
    reports.register("html") {
        required = true
        outputLocation = layout.buildDirectory.file("reports/spotbugs/main/spotbugs.html")
        setStylesheet("fancy-hist.xsl")
    }
}

dependencies {
    spotbugs("com.github.spotbugs:spotbugs:4.8.4")
    spotbugs("com.google.code.findbugs:jsr305:3.0.2")
}

sonarqube {
    properties {
        property("sonar.projectKey", "hashgraph_hedera-sdk-java")
        property("sonar.organization", "hashgraph")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.exclusions", "examples/**")
    }
}
