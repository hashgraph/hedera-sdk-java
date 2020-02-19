# Hedera Java SDK
[![Actions Status](https://github.com/hashgraph/hedera-sdk-java/workflows/Java/badge.svg
)](https://github.com/hashgraph/hedera-sdk-java/actions?query=workflow%3AJava)
![Maven](https://img.shields.io/maven-central/v/com.hedera.hashgraph/sdk)
[![Documentation](https://img.shields.io/badge/javadoc-reference-informational)](https://hashgraph.github.io/hedera-sdk-java/)
[![License: Apache-2.0](https://img.shields.io/badge/license-Apache--2.0-green)](https://github.com/hashgraph/hedera-sdk-java/blob/master/LICENSE)

> The Java SDK for interacting with [Hedera Hashgraph]: the official distributed consensus
> platform built using the hashgraph consensus algorithm for fast, fair and secure
> transactions. Hedera enables and empowers developers to build an entirely new
> class of decentralized applications.

[Hedera Hashgraph]: https://hedera.com/

Hedera Hashgraph communicates using [gRPC]; the Protobufs definitions for the protocol are
available in the [hashgraph/hedera-protobuf] repository.

[gRPC]: https://grpc.io
[hashgraph/hedera-protobuf]: https://github.com/hashgraph/hedera-protobuf

## Usage

#### Maven

```xml
<dependency>
  <groupId>com.hedera.hashgraph</groupId>
  <artifactId>sdk</artifactId>
  <version>1.1.3</version>
</dependency>

<!-- SELECT ONE: -->
<!-- netty transport (for server or desktop applications) -->
<dependency>
  <groupId>io.grpc</groupId>
  <artifactId>grpc-netty-shaded</artifactId>
  <version>1.24.0</version>
</dependency>
<!-- netty transport, unshaded (if you have a matching Netty dependency already) -->
<dependency>
  <groupId>io.grpc</groupId>
  <artifactId>grpc-netty</artifactId>
  <version>1.24.0</version>
</dependency>
<!-- okhttp transport (for lighter-weight applications or Android) -->
<dependency>
  <groupId>io.grpc</groupId>
  <artifactId>grpc-okhttp</artifactId>
  <version>1.24.0</version>
</dependency>
```

#### Gradle

```groovy
implementation 'com.hedera.hashgraph:sdk:1.1.3'

// SELECT ONE:
// netty transport (for high throughput applications)
implementation 'io.grpc:grpc-netty-shaded:1.24.0'
// netty transport, unshaded (if you have a matching Netty dependency already)
implementation 'io.grpc:grpc-netty:1.24.0'
// okhttp transport (for lighter-weight applications or Android)
implementation 'io.grpc:grpc-okhttp:1.24.0'
```

## Contributing to this Project

We welcome participation from all developers!
For instructions on how to contribute to this repo, please
review the [Contributing Guide](CONTRIBUTING.md).

## License Information

Licensed under Apache License,
Version 2.0 – see [LICENSE](LICENSE) in this repo
or [apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)
