# Hedera Java SDK
[![Build Status](https://travis-ci.org/hashgraph/hedera-sdk-java.svg?branch=master)](https://travis-ci.org/hashgraph/hedera-sdk-java)
![Maven](https://img.shields.io/maven-metadata/v/http/central.maven.org/maven2/com/hedera/hashgraph/sdk/maven-metadata.xml.svg)
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
  <version>0.6.0</version>
</dependency>
```

#### Gradle

```groovy
implementation 'com.hedera.hashgraph:sdk:0.6.0'
```

## Contributing to this Project

We welcome participation from all developers!
For instructions on how to contribute to this repo, please
review the [Contributing Guide](CONTRIBUTING.md).

## License Information

Licensed under Apache License,
Version 2.0 – see [LICENSE](LICENSE) in this repo
or [apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)
