plugins {
    id("com.hedera.gradlebuild.sdk.java")
    id("com.hedera.gradlebuild.sdk.publish")
}

dependencies {
    // https://mvnrepository.com/artifact/com.github.spotbugs/spotbugs-annotations
    implementation("com.github.spotbugs:spotbugs-annotations:4.7.3")

    // https://mvnrepository.com/artifact/org.bouncycastle/bcprov-jdk15to18
    implementation("org.bouncycastle:bcprov-jdk15to18:1.76")

    // https://mvnrepository.com/artifact/org.bouncycastle/bcpkix-jdk15to18
    implementation("org.bouncycastle:bcpkix-jdk15to18:1.76")

    // https://mvnrepository.com/artifact/org.slf4j/slf4j-api
    implementation("org.slf4j:slf4j-api:2.0.9")

    implementation("io.grpc:grpc-core:1.57.2")
    implementation("io.grpc:grpc-stub:1.58.0")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("com.esaulpaugh:headlong:10.0.0")

    testImplementation("com.google.errorprone:error_prone_annotations:2.21.1")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("io.github.json-snapshot:json-snapshot:1.0.17")

    testRuntimeOnly("org.slf4j:slf4j-simple:2.0.9")
    testRuntimeOnly("io.grpc:grpc-netty-shaded:1.57.2")
    testRuntimeOnly("org.slf4j:slf4j-nop:2.0.9")

    integrationTestImplementation("com.google.code.findbugs:jsr305:3.0.2")
    integrationTestImplementation("com.esaulpaugh:headlong:10.0.0")
    integrationTestImplementation("org.bouncycastle:bcprov-jdk15to18:1.76")
    integrationTestImplementation("com.google.errorprone:error_prone_annotations:2.21.1")
    integrationTestImplementation("org.assertj:assertj-core:3.24.2")
    integrationTestRuntimeOnly("io.grpc:grpc-netty-shaded:1.57.2")
    integrationTestRuntimeOnly("org.slf4j:slf4j-nop:2.0.9")
}
