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

import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("application")
    id("com.hedera.gradle.java-base")
}

tasks.withType<JavaCompile>().configureEach {
    options.errorprone {
        disable("DefaultPackage")
        disable("SystemOut")
    }
}

tasks.register<RunAllExample>("runAllExamples") {
    sources.from(sourceSets.main.get().java.asFileTree)
    rtClasspath.from(configurations.runtimeClasspath.get() + files(tasks.jar))
}

tasks.addRule("Pattern: run<Example>: Runs an example.") {
    if (startsWith("run")) {
        tasks.register<JavaExec>(this) {
            classpath = configurations.runtimeClasspath.get() + files(tasks.jar)
            mainModule = "com.hedera.hashgraph.examples"
            mainClass = "com.hedera.hashgraph.sdk.examples.${this@addRule.substring("run".length)}Example"

            // NOTE: Uncomment to enable trace logs in the SDK during the examples
            // jvmArgs("-Dorg.slf4j.simpleLogger.log.com.hedera.hashgraph=trace")
        }
    }
}

abstract class RunAllExample : DefaultTask() {
    @get:InputFiles
    abstract val sources: ConfigurableFileCollection

    @get:InputFiles
    abstract val rtClasspath: ConfigurableFileCollection

    @get:Inject
    abstract val exec: ExecOperations

    @TaskAction
    fun runAll() {
        val exampleClasses = sources
            .filter { it.name.endsWith("Example.java") }
            .asSequence()
            .map { it.name.replace(".java", "") }
            .filter { it != "ValidateChecksumExample" } // disabled this example, because it needs user input (but it WORKS)
            .filter { it != "ZeroTokenOperationsExample" } // doesn't work
            .filter { it != "SolidityPrecompileExample" } // doesn't work with hedera-local-node
            .filter { it != "ConsensusPubSubChunkedExample" } // doesn't work on CI
            .toList()

        exampleClasses.forEach { className ->
            println("""

            ---EXECUTING $className:

            """.trimIndent());

            exec.javaexec {
                classpath = rtClasspath
                mainModule = "com.hedera.hashgraph.examples"
                mainClass = "com.hedera.hashgraph.sdk.examples.$className"

                // NOTE: Uncomment to enable trace logs in the SDK during the examples
                // jvmArgs "-Dorg.slf4j.simpleLogger.log.com.hedera.hashgraph=trace"
            }
        }
    }
}
