plugins {
    id("com.hedera.gradle.sdk")
}

dependencies.constraints {
    // Define version of protobuf by dependency coordinates as both
    // have the module name: 'com.google.protobuf'
    val protobufVersion = "3.21.9"
    versions("com.google.protobuf:protobuf-java:$protobufVersion")
    versions("com.google.protobuf:protobuf-javalite:$protobufVersion")
}

moduleInfo {
    version("com.github.spotbugs.annotations", "4.7.3")
    version("com.google.common", "33.0.0-jre")
    version("com.google.errorprone.annotations", "2.21.1")
    version("com.google.gson", "2.10.1")
    version("grpc.protobuf.lite", "1.50.2")
    version("grpc.protobuf", "1.50.2")
    version("headlong", "10.0.0")
    version("io.grpc", "3.2.1")
    version("io.grpc.stub", "1.58.0")
    version("java.annotation", "1.3.2")
    version("org.bouncycastle.pkix", "1.76")
    version("org.bouncycastle.provider", "1.76")
    version("org.slf4j", "2.0.9")
    version("org.slf4j.simple", "2.0.9")

    // Versions only required for tests and examples
    version("grpc.netty.shaded", "1.57.2")
    version("java.dotenv", "5.3.1")
    version("json.snapshot", "1.0.17")
    version("kotlin.stdlib", "1.9.22")
    version("org.apache.commons.lang3", "3.14.0")
    version("org.assertj.core", "3.24.2")
    version("org.wiremock.standalone", "3.4.2")
}

testModuleInfo {
    requires("json.snapshot")
    requires("org.assertj.core")
    requires("org.junit.jupiter.api")
    requires("org.junit.jupiter.params")
    requires("org.mockito")
    requires("org.wiremock.standalone")

    requiresStatic("java.annotation")
    requiresStatic("com.github.spotbugs.annotations")
    requiresStatic("com.google.errorprone.annotations")

    runtimeOnly("grpc.netty.shaded")
    runtimeOnly("org.slf4j.simple")
}

testIntegrationModuleInfo {
    runtimeOnly("grpc.netty.shaded")
    runtimeOnly("org.slf4j.simple")
}
