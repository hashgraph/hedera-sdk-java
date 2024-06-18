plugins {
    id("com.hedera.gradle.sdk-full")
}

// Define dependency constraints for gRPC implementations so that clients automatically get the correct version
dependencies.constraints {
    api("io.grpc:grpc-netty:1.64.0")
    api("io.grpc:grpc-netty-shaded:1.64.0")
    api("io.grpc:grpc-okhttp:1.64.0")
}
