### Creating a fat/uber JAR

To create a fat/uber jar of your Java application that uses the Hedera Java SDK, you need to use the Shadow Gradle plugin:

```groovy
id "com.github.johnrengelman.shadow"
```

and configure it as shown below:
```groovy
tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    group = "shadow"
    from(sourceSets.main.get().output)
    mergeServiceFiles()

    // Defer the resolution  of 'runtimeClasspath'. This is an issue in the shadow
    // plugin that it automatically accesses the files in 'runtimeClasspath' while
    // Gradle is building the task graph. The three lines below work around that.
    inputs.files(project.configurations.runtimeClasspath)
    configurations = emptyList()
    doFirst { configurations = listOf(project.configurations.runtimeClasspath.get()) }

    archiveBaseName.set("archive") // Replace with your preferred name
    manifest {
        attributes["Main-Class"] = "org.example.Main" // Replace with your main class
    }
}
```
