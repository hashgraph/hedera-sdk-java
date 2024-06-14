plugins {
    id("java")
    id("com.diffplug.spotless") version "6.25.0"
    id("org.springframework.boot") version "3.2.3"
}

description = "Hedera SDK TCK Server"
group = "com.hedera.hashgraph.sdk.tck"
version = "0.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.2.3"))
    implementation(platform(project(":sdk-dependency-versions")))

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.thetransactioncompany:jsonrpc2-server:2.0")
    implementation(project(":sdk"))
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testImplementation("org.mockito:mockito-core:3.11.2")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

spotless {
    java {
        endWithNewline()
        palantirJavaFormat()
        target("**/*.java")
        toggleOffOn()
    }
}

tasks.test {
    // discover and execute JUnit5-based tests
    useJUnitPlatform()
}
