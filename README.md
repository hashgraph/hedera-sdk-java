# Hedera Java SDK

[![CircleCI](https://circleci.com/gh/swirlds/hedera-sdk-java.svg?style=shield&circle-token=d288d5d093d2529ad8fbd5d2e8b3e26be22dcaf7)](https://circleci.com/gh/swirlds/hedera-sdk-java)
[![Discord](https://img.shields.io/discord/373889138199494658.svg)](https://hashgraph.com/discord/)

The official Java SDK for interacting with [Hedera Hashgraph](https://hedera.com): the official distributed consensus platform built using the hashgraph consensus algorithm for fast, fair and secure transactions. Hedera enables and empowers developers to build an entirely new class of decentralized applications.

#### Version is 0.1.0

The Hedera Java SDK uses [semantic versioning](https://semver.org/).

Features supported include:

- Micro-payments between two Accounts
- Storing files to Hedera
- Creating [Solidity](https://solidity.readthedocs.io/en/latest/index.html) Smart Contracts
- Executing Smart Contracts

## Getting started

#### Pre-requisites

- [JDK](https://www.oracle.com/technetwork/java/javase/downloads/jdk10-downloads-4416644.html): 10.0.x
- [Maven](https://maven.apache.org/): 3.5.x

#### Installing

From [Eclipse](https://www.eclipse.org/downloads/) or [IntelliJ](https://www.jetbrains.com/idea/):
 
- Right click on `pom.xml`
- `Run As` -> `Maven Install`

From CLI:
```shell
$> mvn install
```

Note that the `.proto` files are compiled with Maven, so the project may initially look full of errors, this is normal.

If there are still some project issues, try a Maven project update and project clean followed by a Maven install.

#### Running the examples

A `node.properties.sample` is provided, copy the file to `node.properties` and update with your account details, the details of the node you want to communicate to and finally, your private and public keys (as hex strings).
This file is ignored by git so all changes will remain local.

#### Javadocs

Javadocs are generated automatically as part of the Maven build (if run from Eclipse, make sure your `JAVA HOME` is set otherwise the build will fail).
They are generated as a `JAR` file which is compiled into the target/ folder.

## More information

To learn more about Hedera visit [The Hedera Site](https://hedera.com).

If you want to contribute please review the [Contributing Guide](https://github.com/hashgraph/hedera-sdk-java/blob/master/CONTRIBUTING.md).

#### Need help?

* Ask questions in [Discord](https://hashgraph.com/discord)
* Open a ticket in GitHub [issue tracker](https://github.com/hashgraph/hedera-sdk-java/issues)

#### License

Copyright (c) 2018-present, Hedera Hashgraph LLC.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.