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

## Environment Set-up

#### Pre-requisites

- [JDK](https://www.oracle.com/technetwork/java/javase/downloads/jdk10-downloads-4416644.html): 10.0.x
- [Maven](https://maven.apache.org/): 3.5.x

   To check the versions of these products from CLI use:

   Java: `java -version`

   Maven: `mvn --version`

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

##### Example location within this repo
There is a main Demo file in each of the three main example folders:
* [account](https://github.com/hashgraph/hedera-sdk-java/blob/master/examples/main/java/com/hedera/account/) folder contains [DemoAccount.java](https://github.com/hashgraph/hedera-sdk-java/blob/master/examples/main/java/com/hedera/account/DemoAccount.java) class.
* [files](https://github.com/hashgraph/hedera-sdk-java/blob/master/examples/main/java/com/hedera/file/) folder contains [DemoFile.java](https://github.com/hashgraph/hedera-sdk-java/blob/master/examples/main/java/com/hedera/file/DemoFile.java) class.
* [contracts](https://github.com/hashgraph/hedera-sdk-java/blob/master/examples/main/java/com/hedera/contracts/) folder contains [DemoContract.java](https://github.com/hashgraph/hedera-sdk-java/blob/master/examples/main/java/com/hedera/contracts/DemoContract.java) class.

These Demo files contain working examples of account, file and smart-contract APIs. As such they provide a good starting point for developers who wish to familiarise themselves with the Hedera SDK for Java.

#### Javadocs
Javadocs are generated automatically as part of the Maven build (if run from Eclipse, make sure your `JAVA HOME` is set otherwise the build will fail).

They are generated as a `JAR` file which is compiled into the target/ folder.

## Building a simple test with the Hedera SDK for Java
Code snippets used in this document are excerpt from the full examples included in this repo. Those examples can be found [here](https://github.com/hashgraph/hedera-sdk-java/tree/master/examples/main/java/com/hedera) in their entirety.

### Prerequisites for using the SDK

#### Access to the Hedera mainnet or a Hedera testnet
In order to be able to use the Hedera SDK for Java you must have access to a testnet or to the Hedera mainnet. Access to mainnet and testnets are currently restricted to Hedera Hashgraph Council members and partners.

Hackathon attendees will also gain temporary access to a Hedera testnet. If you wish to participate in a Hedera hackathon, please register on the [Hedera18](https://www.hedera18.com/) page.

##### Addresses of testnet nodes

The administrator who gives you access to a testnet will also provide you with the following details associated with at least one node:

* __IP address__ and/or __DNS name__ – providing IP connectivity to that node
* __Port number__ – The specific port on the node to which queries and transactions must be sent
* __Account ID__ – the Hedera account number associated with the node. This is required in order to issue transactions and queries. It determines the account to which node-fees will be paid. Hedera account IDs are made up of three int64 numbers separated by colons (e.g. 0:0:3) The three numbers represent __Shard-number__, __Realm-number__ and __Account-number__ respectively. Shards and Realms are not yet in use, so you should expect Account IDs to start with two zeros for the present time.

__NOTE__: This information should be added to your `node.properties` file (see above) if you wish to run the example code provided.

#### Public/Private Key Pairs

Hedera accounts must be associated with cryptographic keys. A complete explanation of the ED25519 key pairs can be generated using the [Hedera Keygen utility](https://github.com/hashgraph/hedera-keygen-java). A complete explanation of the key generation process is documented in the [readme](https://github.com/hashgraph/hedera-keygen-java/blob/master/README.md) file in that repo. A minimal version of that process will be shown below.

__NOTE__: This information should be added to your `node.properties` file (see above) if you wish to run the example code provided.

### Using the Hedera SDK for Java

The following steps describe some first steps using the SDK:

1. Generate a Key Pair
2. Create a new java project
3. Create a Hedera account
4. Retrieve the balance of a Hedera account
5. Transfer hbars between Hedera accounts

#### Generating a Key Pair
Download the [sdk-keygen-jar](https://github.com/hashgraph/hedera-keygen-java/blob/master/target/sdk-keygen-1.0.jar) to a folder in your development environment.

Open that folder using terminal and execute the following command to generate a public/private key pair based on a system-generated random seed:
```Shell
java -jar sdk-keygen-1.0.jar
```

##### Example Output
```
Your key pair is:
Public key:
302a300506032b6570032100a1a16c812bdc3b260d3f7b42c33b8f80337fbfd3ac0df703015b096b55e99d9f
Secret key:
302e020100300506032b657004220420975b637b1f648ae04e7e6109542f57cb58667c50e2366b91e76199bd458e2620
Recovery word list:
[ink, enable, opaque, clap, make, toe, brine, tundra, cater, Joe, small, run, Seoul, grand, atom, crush, circus, abbey, vacuum, whim, hollow, afar]
```
Copy this information into a safe place as you will need these keys below.

Should you wish to specify your own seed, please refer to the Hedera KeyGen [readme](https://github.com/hashgraph/hedera-keygen-java/blob/master/README.md) file.

#### Create a new Java project
Open your IDE of choice (Eclipse, IntelliJ, VSCode...)

Once the maven install has been completed, you should be able to locate a new jar-file __sdk-0.1.0.jar__ within the __hedera-sdk-java/target/__ folder.

Once you have added that jar file to your project, you should be able to import classes from the SDK for use within your application.

#### Create a Hedera account
Documented walk-through to follow shortly.

#### Retrieve the balance of a Hedera account
Documented walk-through to follow shortly.

#### Transfer *hbars* between Hedera accounts
Documented walk-through to follow shortly.

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
