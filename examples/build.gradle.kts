// SPDX-License-Identifier: Apache-2.0
plugins {
    id("org.hiero.gradle.module.application")
    id("org.gradlex.java-module-dependencies")
}

mainModuleInfo {
    runtimeOnly("io.grpc.netty.shaded")
    runtimeOnly("org.slf4j.simple")
}

// 'protobuf' implementation provided through 'sdk' as it differs between 'sdk' and 'sdk-full'
dependencyAnalysis {
    issues {
        all { onUsedTransitiveDependencies { exclude("com.google.protobuf:protobuf-javalite") } }
    }
}

dependencies.constraints {
    implementation("com.google.guava:guava:33.3.1-android")
    implementation("io.github.cdimascio:dotenv-java:3.0.2")
    implementation("com.hedera.hashgraph:sdk:2.46.0")
    implementation("com.hedera.hashgraph:sdk-full:2.46.0")
}

tasks.register<RunAllExample>("runAllExamples") {
    workingDirectory = rootDir
    sources.from(sourceSets.main.get().java.asFileTree)
    rtClasspath.from(configurations.runtimeClasspath.get() + files(tasks.jar))
}

tasks.addRule("Pattern: run<Example>: Runs an example.") {
    if (startsWith("run")) {
        tasks.register<JavaExec>(this) {
            workingDir = rootDir
            classpath = configurations.runtimeClasspath.get() + files(tasks.jar)
            mainModule = "com.hedera.hashgraph.examples"
            mainClass =
                "com.hedera.hashgraph.sdk.examples.${this@addRule.substring("run".length)}Example"
        }
    }
}

abstract class RunAllExample : DefaultTask() {
    @get:InputFiles abstract val sources: ConfigurableFileCollection

    @get:InputFiles abstract val rtClasspath: ConfigurableFileCollection

    @get:Internal abstract val workingDirectory: RegularFileProperty

    @get:Inject abstract val exec: ExecOperations

    @TaskAction
    fun runAll() {
        val exampleClasses =
            sources
                .filter { it.name.endsWith("Example.java") }
                .asSequence()
                .map { it.name.replace(".java", "") }
                .filter {
                    it != "ValidateChecksumExample"
                } // disabled this example, because it needs user input (but it WORKS)
                .filter {
                    it != "ConsensusPubSubChunkedExample"
                } // is flaky on local-node env, will be investigated
                .filter {
                    it != "InitializeClientWithMirrorNetworkExample"
                } // disabled - cannot run on localnode
                .toList()

        exampleClasses.forEach { className ->
            println(
                """

            ---EXECUTING $className:

            """
                    .trimIndent()
            )

            exec.javaexec {
                workingDir = workingDirectory.get().asFile
                classpath = rtClasspath
                mainModule = "com.hedera.hashgraph.examples"
                mainClass = "com.hedera.hashgraph.sdk.examples.$className"

                // NOTE: Uncomment to enable trace logs in the SDK during the examples
                // jvmArgs "-Dorg.slf4j.simpleLogger.log.com.hedera.hashgraph=trace"
            }
        }
    }
}
