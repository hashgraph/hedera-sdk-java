plugins {
    id("com.hedera.gradle.base.lifecycle")
    id("java")
}

dependencies {
    implementation(project(":sdk"))
    implementation("io.grpc:grpc-protobuf")
}
