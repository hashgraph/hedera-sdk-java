# Hedera™ Hashgraph Java SDK

![](https://img.shields.io/badge/java-7%2B-blue?style=flat-square)
![](https://img.shields.io/badge/android-19%2B-blue?style=flat-square)
[![](https://img.shields.io/github/workflow/status/hashgraph/hedera-sdk-java/Java/develop?style=flat-square)](https://github.com/hashgraph/hedera-sdk-java/actions)
[![](https://img.shields.io/maven-central/v/com.hedera.hashgraph/sdk/2?label=maven&style=flat-square)](https://search.maven.org/artifact/com.hedera.hashgraph/sdk)

> The Java SDK for interacting with Hedera Hashgraph: the official distributed
> consensus platform built using the hashgraph consensus algorithm for fast,
> fair and secure transactions. Hedera enables and empowers developers to
> build an entirely new class of decentralized applications.

## Install

**NOTE**: v1 of the SDK is deprecated and support will be discontinued after October 2021. Please install the latest version 2.0.x or migrate from v1 to the latest 2.0.x version. You can reference the [migration documentation](/MIGRATING_V1.md).

#### Gradle

Select _one_ of the following depending on your target platform.

```groovy
// Android, Corda DJVM, Java 7+
implementation 'com.hedera.hashgraph:sdk-jdk7:2.20.0-beta.1'

// Java 9+, Kotlin
implementation 'com.hedera.hashgraph:sdk:2.20.0-beta.1'
```

Select _one_ of the following to provide the gRPC implementation.

```groovy
// netty transport (for high throughput applications)
implementation 'io.grpc:grpc-netty-shaded:1.46.0'

// netty transport, unshaded (if you have a matching Netty dependency already)
implementation 'io.grpc:grpc-netty:1.46.0'

// okhttp transport (for lighter-weight applications or Android)
implementation 'io.grpc:grpc-okhttp:1.46.0'
```

Select _one_ of the following to enable or disable Simple Logging Facade for Java (SLFJ4).

```groovy
// Enable logs
implementation 'org.slf4j:slf4j-simple:2.0.3'

// Disable logs
implementation 'org.slf4j:slf4j-nop:2.0.3'

```



#### Maven

Select _one_ of the following depending on your target platform.

```xml
<!-- Android, Corda DJVM, Java 7+ -->
<dependency>
  <groupId>com.hedera.hashgraph</groupId>
  <artifactId>sdk-jdk7</artifactId>
  <version>2.20.0-beta.1</version>
</dependency>

<!-- Java 9+, Kotlin -->
<dependency>
  <groupId>com.hedera.hashgraph</groupId>
  <artifactId>sdk</artifactId>
  <version>2.20.0-beta.1</version>
</dependency>
```

Select _one_ of the following to provide the gRPC implementation.

```xml
<!-- netty transport (for server or desktop applications) -->
<dependency>
  <groupId>io.grpc</groupId>
  <artifactId>grpc-netty-shaded</artifactId>
  <version>1.46.0</version>
</dependency>

<!-- netty transport, unshaded (if you have a matching Netty dependency already) -->
<dependency>
  <groupId>io.grpc</groupId>
  <artifactId>grpc-netty</artifactId>
  <version>1.46.0</version>
</dependency>

<!-- okhttp transport (for lighter-weight applications or Android) -->
<dependency>
  <groupId>io.grpc</groupId>
  <artifactId>grpc-okhttp</artifactId>
  <version>1.46.0</version>
</dependency>
```

## Usage

Examples of several potential use cases and workflows are available
within the repository in [`examples/`](./examples/src/main/java).

 * [Create Account](./examples/src/main/java/CreateAccountExample.java)

 * [Transfer Hbar](./examples/src/main/java/TransferCryptoExample.java)

 * [Hedera Consensus Service (HCS)](./examples/src/main/java/ConsensusPubSubExample.java)

## Development

### Dependencies

 * [Java Development Kit (JDK)](https://adoptopenjdk.net/) v14+ (note this is to _build_, not run)

### Compile

```sh
$ ./gradlew compileJava
```

### Unit Test

```sh
$ ./gradlew test
```

### Integration Test

The easiest way to run integration tests is by providing network and operator information in a configuration file.
This configuration file is passed into system properties.

```sh
$ ./gradlew integrationTest -PCONFIG_FILE="<ConfigurationFilePath>"
```

An example configuration file can be found in the repo here:

[sdk/src/test/resources/client-config-with-operator.json](sdk/src/test/resources/client-config-with-operator.json)

The format of the configuration file should be as follows:

```
{
    "network": {
        "<NodeAddress>": "<NodeAccountId>",
        ...
    },
    "operator": {
        "accountId": "<shard.realm.num>",
        "privateKey": "<PrivateKey>"
    }
}
```

If a configuration file is not provided, `OPERATOR_ID` and `OPERATOR_KEY` must be passed into system properties
and integration tests will run against the Hedera test network.

```sh
$ ./gradlew integrationTest -POPERATOR_ID="<shard.realm.num>" -POPERATOR_KEY="<PrivateKey>"
```

`HEDERA_NETWORK` can optionally be used to use `previewnet`.  This System Property can only be set to `previewnet`.

```sh
$ ./gradlew integrationTest -POPERATOR_ID="<shard.realm.num>" -POPERATOR_KEY="<PrivateKey>" -PHEDERA_NETWORK="previewnet"
```

Note: It is also possible to use a custom network in a configuration file and pass `OPERATOR_ID` and `OPERATOR_KEY`
into system properties.

An example configuration file containing only network information can be found in the repo here:

[sdk/src/test/resources/client-config.json](sdk/src/test/resources/client-config.json)

### Examples

Requires `OPERATOR_ID` and `OPERATOR_KEY` to be in a .env file in the examples directory.   Many examples run against
the Hedera test network.

```sh
$ ./gradlew -q example:run<NameOfExample>
$ ./gradlew -q example:runGenerateKey
```

## Contributing to this Project

We welcome participation from all developers!
For instructions on how to contribute to this repo, please
review the [Contributing Guide](CONTRIBUTING.md).

## License Information

Licensed under Apache License,
Version 2.0 – see [LICENSE](LICENSE) in this repo
or [apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0).
