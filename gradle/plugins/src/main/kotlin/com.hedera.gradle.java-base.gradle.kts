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
    id("jacoco")
    id("org.gradlex.java-module-dependencies")
    id("com.hedera.gradle.base")
    id("com.hedera.gradle.repositories")
    id("com.hedera.gradle.patch-modules")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
        vendor = JvmVendorSpec.ADOPTIUM
    }
}

jacoco {
    toolVersion = "0.8.8"
}

jvmDependencyConflicts {
    consistentResolution.platform("com.hedera.hashgraph:sdk-dependency-versions")
}

val deactivatedCompileLintOptions =
    listOf(
        "module", // module not found when doing 'exports to ...'
        "serial", // serializable class ... has no definition of serialVersionUID
        "processing", // No processor claimed any of these annotations: ...
        "try", // auto-closeable resource ignore is never referenced... (AutoClosableLock)
        "missing-explicit-ctor", // class ... declares no explicit constructors
        "removal",
        "deprecation",
        "overrides", // overrides equals, but neither it ... overrides hashCode method
        "unchecked",
        "rawtypes",
        "exports",
        "dep-ann"
    )

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Werror")
    options.compilerArgs.add("-Xlint:all,-" + deactivatedCompileLintOptions.joinToString(",-"))
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
    filePermissions { unix("0664") }
    dirPermissions { unix("0775") }
}

tasks.buildDependents { setGroup(null) }

tasks.buildNeeded { setGroup(null) }

tasks.jar { setGroup(null) }

tasks.test { group = "build" }

tasks.checkAllModuleInfo { group = "build" }

sourceSets.all {
    // Remove 'classes' tasks from 'build' group to keep it cleaned up
    tasks.named(classesTaskName) { group = null }
}
