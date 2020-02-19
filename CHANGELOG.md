# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## v1.1.3

### Added

 * Add additional error classes to allow more introspection on errors:
    * `HederaPrecheckStatusException` - Thrown when the transaction fails at the node (the precheck)
    * `HederaReceiptStatusException` - Thrown when the receipt is checked and has a failing status. The error object contains the full receipt.
    * `HederaRecordStatusException` - Thrown when the record is checked and it has a failing status. The error object contains the full record.

### Fixed

 * Add missing `setTransferAccountId` and `setTransferContractId` methods to
   `ContractDeleteTransaction`

 * Override `executeAsync` to sign by the operator (if not already)

### Deprecated

 * Deprecate `toSolidityAddress` and `fromSolidityAddress` on `FileId`

## v1.1.2

### Fixed

 * https://github.com/hashgraph/hedera-sdk-java/issues/350

## v1.1.1

### Fixed

 * https://github.com/hashgraph/hedera-sdk-java/issues/342

## v1.1.0

### Added

Add support for Hedera Consensus Service (HCS).

 * Add `ConsensusTopicCreateTransaction`, `ConsensusTopicUpdateTransaction`, `ConsensusTopicDeleteTransaction`, and `ConsensusMessageSubmitTransaction` transactions

 * Add `ConsensusTopicInfoQuery` query (returns `ConsensusTopicInfo`)

 * Add `MirrorClient` and `MirrorConsensusTopicQuery` which can be used to listen for HCS messages from a mirror node

## v1.0.0

Removed all deprecated APIs from v1.0.0.

### Changed

 * Instead of returning `ResponseCodeEnum` from the generated protos, return a new `Status` type
   that wraps that and provides some Java conveniences.

 * Rename `HederaException` to `HederaStatusException`

 * Rename `QueryBuilder.MaxPaymentExceededException` to `MaxQueryPaymentExceededException`

 * Change `AccountBalanceQuery` to return `Hbar` (instead of `Long`)

 * Change `ContractGetBytecodeQuery` to return `byte[]` (instead of the internal proto type)

 * Remove `GetBySolidityIdQuery`. Instead, you should use `AccountId.toSolidityAddress`.

 * Change `ContractRecordsQuery` to return `TransactionRecord[]`

## v0.9.0

### Changed

All changes are not immediately breaking as the previous method still should exist and be working. The previous methods are flagged as deprecated and will be removed upon `v1.0`.

 * Transactions and queries do not take `Client` in the constructor; instead, `Client` is passed to `execute`.

 * Removed `Transaction.executeForReceipt` and `Transaction.executeForRecord`

    These methods have been identified as harmful as they hide too much. If one fails, you do not know if the transaction failed to execute; or, the receipt/record could not be retrieved. In a mission-critical application, that is, of course, an important distinction.

    Now there is only `Transaction.execute` which returns a `TransactionId`. If you don't care about waiting for consensus or retrieving a receipt/record in your application, you're done. Otherwise you can now use any `TransactionId` and ask for the receipt/record (with a stepped retry interval until consensus) with `TransactionId.getReceipt` and `TransactionId.getRecord`.

    v0.8.x and below

    ```java
    AccountId newAccountId = new AccountCreateTransaction(hederaClient)
        .setKey(newKey.getPublicKey())
        .setInitialBalance(1000)
        .executeForReceipt() // TransactionReceipt
        .getAccountId();
    ```

    v0.9.x

    ```java
    AccountId newAccountId = new AccountCreateTransaction()
        .setKey(newKey.getPublicKey())
        .setInitialBalance(1000)
        .execute(hederaClient) // TranactionId
        .getReceipt(hederaClient) // TransactionReceipt
        .getAccountId();
    ```

 * `TransactionReceipt`, `AccountInfo`, `TransactionRecord`, etc. now expose public final fields instead of getters (where possible and it makes sense).

 * Rename `getCallResult` and `getCreateResult` to `getContractExecuteResult` and `getContractCreateResult` for consistency

 * `TransactionBuilder.setMemo` is renamed to `TransactionBuilder.setTransactionMemo` to avoid confusion
   as there are 2 other kinds of memos on transactions

 * `CallParams` is removed in favor of `ContractFunctionParams` and closely mirrors type names from solidity
    * `addInt32`
    * `addInt256Array`
    * `addString`
    * etc.

 * `ContractFunctionResult` now closely mirrors the solidity type names
   * `getInt32`
   * etc.

 * `setFunctionParams(params)` on `ContractCallQuery` and `ContractExecuteTransaction` is now
   `setFunction(name, params)`

### Added

 * `TransactionId.getReceipt`

 * `TransactionId.getRecord`

 * `FileId.ADDRESS_BOOK`, `FileId.FEE_SCHEDULE`, `FileId.EXCHANGE_RATES`

 * Experimental support for the Hedera Consensus Service (HCS). HCS is not yet generally available but if you have access
   the SDK can work with the current iteration of it. Due to its experimental nature, a system property must be set before use.

    ```java
    System.setPropery("com.hedera.hashgraph.sdk.experimental", "true")
    ```

 * `Client.forTestnet` makes a new client configured to talk to TestNet (use `.setOperator` to set an operater)

 * `Client.forMainnet` makes a new client configured to talk to Mainnet (use `.setOperator` to set an operater)

### Fixes

 * `FileCreateTransaction` sets a default expiration time; fixes `AUTORENEW_DURATION_NOT_IN_RANGE`.

 * `BUSY` is now internally retried in all cases.

 * The maximum query payment is now defaulted to 1 Hbar. By default, just before a query is executed we ask Hedera how much the query will cost and if it costs under the defined maximum, an exact payment is sent.

### Removed

 * `Transaction` and `Query` types related to claims

## v0.8.0

Fixes compatibility with Android.

## Breaking Changes

 * The `Key` interface has been renamed to `PublicKey`
 * You are now required to depend on the gRPC transport dependency for your specific environment

#### Maven

```xml
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
// SELECT ONE:
// netty transport (for high throughput applications)
implementation 'io.grpc:grpc-netty-shaded:1.24.0'
// netty transport, unshaded (if you have a matching Netty dependency already)
implementation 'io.grpc:grpc-netty:1.24.0'
// okhttp transport (for lighter-weight applications or Android)
implementation 'io.grpc:grpc-okhttp:1.24.0'
```
