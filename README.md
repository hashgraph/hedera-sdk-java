# Hedera Java SDK

[![CircleCI](https://circleci.com/gh/swirlds/hedera-sdk-java.svg?style=shield&circle-token=d288d5d093d2529ad8fbd5d2e8b3e26be22dcaf7)](https://circleci.com/gh/swirlds/hedera-sdk-java)
[![Discord](https://img.shields.io/discord/373889138199494658.svg)](https://hashgraph.com/discord/)

The Java SDK for interacting with [Hedera Hashgraph](https://hedera.com): the official distributed consensus platform built using the hashgraph consensus algorithm for fast, fair and secure transactions. Hedera enables and empowers developers to build an entirely new class of decentralized applications.

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
A `node.properties.sample` is provided, copy the file to `node.properties` and update with your account details, the details of the node you want to communicate to, and your private/public keys (as hex strings).

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
In order to be able to use the Hedera SDK for Java you must have access to a testnet or to the Hedera mainnet. Access to mainnet and testnets are currently restricted.

Temporary testnets (or "Flashnets") may become available for specific events and engagements such as hackathons.

To get access to any Hedera network you must first create a Hedera Profile in the [Hedera Portal](https://portal.hedera.com), then enter an access or promo code that was received from the Hedera team.

##### Addresses of testnet nodes

After you join a network through the Hedera Portal, you will be provided information required connect to that network, specifically:

* __IP address__ and/or __DNS name__ – providing IP connectivity to that node
* __Port number__ – The specific port on the node to which queries and transactions must be sent
* __Account ID__ – the Hedera account number associated with the node. This is required in order to issue transactions and queries. It determines the account to which node-fees will be paid. Hedera account IDs are made up of three int64 numbers separated by colons (e.g. 0:0:3) The three numbers represent __Shard-number__, __Realm-number__ and __Account-number__ respectively. Shards and Realms are not yet in use, so you should expect Account IDs to start with two zeros for the present time.

__NOTE__: This information should be added to your `node.properties` file (see above) if you wish to run the example code provided.

#### Public/Private Key Pairs

Hedera accounts must be associated with cryptographic keys. ED25519 key pairs can be generated using the [Hedera Keygen utility](https://github.com/hashgraph/hedera-keygen-java). A complete explanation of the key generation process is documented in the [readme](https://github.com/hashgraph/hedera-keygen-java/blob/master/README.md) file in that repo. A minimal version of that process will be shown below.

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
In the interest of clarity, these examples assume a sunny-day scenario and do not include exception-handling logic. Refer to the main [examples](https://github.com/hashgraph/hedera-sdk-java/tree/master/examples/main/java/com/hedera) folder for more resilient code.

This is the definition of the simple version of the `create` method in the `HederaAccount` class:

```java
public HederaTransactionResult create(long shardNum,
                                      long realmNum,
                                      byte[] publicKey,
                                      KeyType keyType,
                                      long initialBalance,
                                      HederaAccountCreateDefaults defaults
                                      )
                                      throws InterruptedException
```

The purpose of each of the parameters is as follows:

`shardNum` - the shard number for the new account. Note that this is not currently used, and should be set to 0 at present.

`realmNum` - the realm number for the new account. Note that this is not currently used, and should be set to 0 at present.

`publicKey` - the public key for the new account. This should be set to the __Public Key__ generated using the Hedera Key Generation Tool (see above).

`keyType` - The type of cryptographic key used by this account. In future, a variety of standards will be supported; at present only ED25519 keys are supported.

`initialBalance` - A Hedera account must contain *hbars*  on creation. This parameter describes that opening balance in *TinyBars*.
__Note__:  100,000,000 *TinyBars* is equivalent to 1 *hbar*.

`defaults` - The Hedera SDK for Java makes extensive use of defaults parameters to maximise reuse and readability. These defaults help new developers to get started without the need to understand all of the necessary parameters in detail. Once you are familiar with basic functionality of each method, additional behaviour can be unlocked by modifying these defaults.

##### Using HederaAccount.create

Utility functions have been provided within the examples. The first of these that we should use configures default settings for all transactions and queries.

__Note__: For these example steps to function as expected, you must have updated the `node.properties` file as described above. The `pubkey` + `privkey` and `payingAccount...` parameters are used to determine the account from which *hbars* are transferred.

```java
// setup a set of defaults for query and transactions
HederaTransactionAndQueryDefaults txQueryDefaults = new HederaTransactionAndQueryDefaults();
txQueryDefaults = ExampleUtilities.getTxQueryDefaults();
```

In order to create a Hedera account, an initial balance must be transferred into the new account from an existing account. The `exampleUtilities.java` package retrieves details of the "source" or paying account from the `node.properties` file.

A `HederaAccount` variable must be defined and associated with the `txQueryDefaults` we just created.

```java
HederaAccount account1 = new HederaAccount();

// setup transaction/query defaults (durations, etc...)
account1.txQueryDefaults = txQueryDefaults;
```

To keep things simple in this example, a cryptographic record of the transaction is not required. In the following code-snippet, a new cryptographic private/public key pair is generated for the new account, specifying a `KeyType` of ED25519. This is equivalent to generating a key pair using the Hedera Key Generation utility. It is worth making a note of those public and private keys.

```java
account1.txQueryDefaults.generateRecord = false;
HederaCryptoKeyPair account1Key = new HederaCryptoKeyPair(KeyType.ED25519);
```

Now that everything is set up correctly, the following statement should create a Hedera account by transferring 100,000 *TinyBars* from the paying account defined in `node.properties` into the new account.

```java
account1 = AccountCreate.create(account1, account1Key, 100000);
```

#### Retrieve the balance of a Hedera account
Assuming that you have completed the steps above, the following statement will retrieve the balance of the account by querying the network.

```java
long balance1 = account1.getBalance();
```

#### Transfer *hbars* between Hedera accounts
To transfer *hbars* from one account to another, a second account is required. The following code snippet replicates the steps undertaken above to create a second account, and assumes that this code will be added to the steps above.

```java
HederaAccount account2 = new HederaAccount();

// setup transaction/query defaults (durations, etc...)
account2.txQueryDefaults = txQueryDefaults;

account2.txQueryDefaults.generateRecord = false;
HederaCryptoKeyPair account2Key = new HederaCryptoKeyPair(KeyType.ED25519);

account2 = AccountCreate.create(account2, account1Key, 100000);
```

At this stage, two accounts: `account1` and `account2` – each holding 100,000 *TinyBars* – have been created.

In the supplied examples, the `txQueryDefaults` object contains details of the original paying account used to fund the opening of both accounts; these defaults were read from the `node.properties` file.

In order to transfer *TinyBars* from `account1` to `account2` we must override that behaviour. This code snippet sets the default paying account to `account1`.

```java
account1.txQueryDefaults.payingAccountID = account1.getHederaAccountID();
account1.txQueryDefaults.payingKeyPair = account1Key;
```

To send the transfer transaction to Hedera, transferring 10,000 *TinyBars* from `account1` to `account2` the following code should be used:

```java
AccountSend.send(account1, account2, 10000);
```
To verify that `account1` now contains 90,000 *TinyBars* and `account2` contains 110,000 *TinyBars* the following instructions should suffice.

```java
long balance1 = account1.getBalance();
long balance2 = account2.getBalance();
```
__Note__: In the event you're not waiting for consensus on the transfer transaction, it's possible that the balances will initially show no change. Adding a small delay between the transfer and the balance queries will ensure the correct values are returned. Ideally, you would ask for a receipt and check transaction status following the transfer transaction before querying the updated balances. This is shown in the examples contained within the SDK.

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
