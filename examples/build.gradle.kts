plugins {
    id("com.hedera.gradle.examples.java")
}

mainModuleInfo {
    runtimeOnly("io.grpc.netty.shaded")
    runtimeOnly("org.slf4j.simple")
}

dependencies.constraints {
    implementation("com.hedera.hashgraph:sdk:2.35.0")
    implementation("com.hedera.hashgraph:sdk-full:2.35.0")
}
