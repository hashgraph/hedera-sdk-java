plugins {
    id("com.hedera.gradlebuild.sdk.java")
    id("com.hedera.gradlebuild.sdk.publish")
}

moduleInfo {
    version("com.github.spotbugs.annotations", "4.7.3")
    version("com.google.common", "33.0.0-jre")
    version("com.google.errorprone.annotations", "2.21.1")
    version("com.google.gson", "2.10.1")
    version("com.google.protobuf", "3.21.9")
    version("grpc.protobuf.lite", "1.46.0")
    version("headlong", "10.0.0")
    version("io.grpc", "3.2.1")
    version("io.grpc.stub", "1.58.0")
    version("java.annotation", "1.3.2")
    version("org.bouncycastle.pkix", "1.76")
    version("org.bouncycastle.provider", "1.76")
    version("org.slf4j", "2.0.9")
    version("org.slf4j.simple", "2.0.9")

    // Versions only required for tests and examples
    version("com.fasterxml.jackson.core", "2.16.1")
    version("java.dotenv", "5.3.1")
    version("json.snapshot", "1.0.17")
    version("kotlin.stdlib", "1.9.22")
    version("org.assertj.core", "3.24.2")
    version("org.apache.commons.lang3", "3.14.0")
}

testModuleInfo {
    requires("org.assertj.core")
    requires("org.junit.jupiter.api")
    requires("org.junit.jupiter.params")
    requires("org.mockito")
    requires("json.snapshot")

    requiresStatic("java.annotation")
    requiresStatic("com.github.spotbugs.annotations")
    requiresStatic("com.google.errorprone.annotations")

    runtimeOnly("org.slf4j.simple")
}

integrationTestModuleInfo {
    runtimeOnly("org.slf4j.simple")
}
