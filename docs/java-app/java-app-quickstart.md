## Get started

> Please note that JDK 17 is required. The Temurin builds of [Eclipse Adoptium](https://adoptium.net/) are strongly recommended.

To get started with a Java project, you'll need to add the following **three** dependencies:

**1. Hedera Java™ SDK:**

_Gradle:_
```groovy
implementation 'com.hedera.hashgraph:sdk:2.33.0'
```

_Maven:_
```xml
<dependency>
  <groupId>com.hedera.hashgraph</groupId>
  <artifactId>sdk</artifactId>
  <version>2.33.0</version>
</dependency>
```

**2. gRPC implementation** _(select one of the following)_**:**

_Gradle:_
```groovy
// netty transport (for high throughput applications)
implementation 'io.grpc:grpc-netty-shaded:1.57.2'
```
```groovy
// netty transport, unshaded (if you have a matching Netty dependency already)
implementation 'io.grpc:grpc-netty:1.57.2'
```
```groovy
// okhttp transport (for lighter-weight applications or Android)
implementation 'io.grpc:grpc-okhttp:1.57.2'
```

_Maven:_
```xml
<!-- netty transport (for server or desktop applications) -->
<dependency>
  <groupId>io.grpc</groupId>
  <artifactId>grpc-netty-shaded</artifactId>
  <version>1.57.2</version>
</dependency>
```
```xml
<!-- netty transport, unshaded (if you have a matching Netty dependency already) -->
<dependency>
  <groupId>io.grpc</groupId>
  <artifactId>grpc-netty</artifactId>
  <version>1.57.2</version>
</dependency>
```
```xml
<!-- okhttp transport (for lighter-weight applications or Android) -->
<dependency>
  <groupId>io.grpc</groupId>
  <artifactId>grpc-okhttp</artifactId>
  <version>1.57.2</version>
</dependency>
```

**3. Simple Logging Facade for Java** _(select one of the following to enable or disable logs)_**:**

_Gradle:_
```groovy
// Enable logs
implementation 'org.slf4j:slf4j-simple:2.0.9'
```
```groovy
// Disable logs
implementation 'org.slf4j:slf4j-nop:2.0.9'
```

_Maven:_
```xml
<!-- Enable logs -->
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-simple</artifactId>
    <version>2.0.9</version>
</dependency>
```
```xml
<!-- Disable logs -->
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-nop</artifactId>
    <version>2.0.9</version>
</dependency>
```

## Additional useful information
### Fat/uber JAR
[Creating a fat/uber JAR guide](create-jar-guide.md) covers how to properly set up Shadow Gradle plugin
to create a fat/uber JAR of your Java application that uses the Hedera™ Java SDK.

## Next steps
To make it easier to start your Java project using the Hedera™ Java SDK,
we recommend checking out the [Java examples](../../examples/README.md).
These examples show different uses and workflows,
giving you valuable insights into how you can use the Hedera platform in your projects.
They will also help you explore the capabilities of the Hedera™ Java SDK
and start your project confidently.
