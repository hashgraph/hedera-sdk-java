plugins {
    id("com.hedera.gradlebuild.examples-java")
}

dependencies {
    implementation(project(":sdk"))

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.slf4j:slf4j-simple:2.0.9")
    implementation("io.grpc:grpc-netty-shaded:1.57.2")
    implementation("io.github.cdimascio:java-dotenv:5.3.1")
    implementation("com.google.errorprone:error_prone_core:2.21.1")
}
