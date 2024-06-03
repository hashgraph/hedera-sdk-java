plugins {
    id("com.hedera.gradle.sdk")
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
