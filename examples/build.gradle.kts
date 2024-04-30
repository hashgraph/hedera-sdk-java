plugins {
    id("com.hedera.gradle.examples.java")
}

mainModuleInfo {
    runtimeOnly("grpc.netty.shaded")
    runtimeOnly("org.slf4j.simple")
}
