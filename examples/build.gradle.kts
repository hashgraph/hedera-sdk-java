plugins {
    id("com.hedera.gradlebuild.examples.java")
}

mainModuleInfo {
    runtimeOnly("grpc.netty.shaded")
    runtimeOnly("org.slf4j.simple")
}
