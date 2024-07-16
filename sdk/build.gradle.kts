plugins {
    id("com.hedera.gradle.sdk")
}

// Define dependency constraints for gRPC implementations so that clients automatically get the correct version
dependencies.constraints {
    api("io.grpc:grpc-netty:1.64.0")
    api("io.grpc:grpc-netty-shaded:1.64.0")
    api("io.grpc:grpc-okhttp:1.64.0")
}

testModuleInfo {
    requires("json.snapshot")
    requires("org.assertj.core")
    requires("org.junit.jupiter.api")
    requires("org.junit.jupiter.params")
    requires("org.mockito")

    requiresStatic("java.annotation")
    requiresStatic("com.github.spotbugs.annotations")
    requiresStatic("com.google.errorprone.annotations")

    runtimeOnly("io.grpc.netty.shaded")
    runtimeOnly("org.slf4j.simple")
}

testIntegrationModuleInfo {
    runtimeOnly("io.grpc.netty.shaded")
    runtimeOnly("org.slf4j.simple")
}
