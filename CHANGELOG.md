# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## v2.0.2

### Renamed `Ed25519PublicKey` -> `PublicKey`
    * Added `boolean verify(byte[], byte[])`
        * Verifies a message was signe by the respective private key.
    * Added `boolean verifyTransaction(Transaction)`
        * Verifies the transaction was signed by the respective private key.
    * Removed `Key toKeyProto()`
    * Removed `boolean hasPrefix()`
    * Removed `SignatureCase getSignatureCase()`

### Renamed `Ed25519PrivateKey` -> `PrivateKey`
    * Added `byte[] signTransaction(Transaction)`
        * Signs the `Transaction` and returns the signature.
    * Added `PublicKey getPublicKey()`
    * Added `PrivateKey fromLegacyMnemonic(byte[])`
    * Renamed `boolean suppotsDeriviation()` -> `boolean isDerivable()`
    * Removed `PrivateKey generate(SecurRandom)`
    * Removed `PrivateKey fromKeystore(Keystore)`
        * Use `Keystore.getEd25519()` instead.
    * Removed `PrivateKey readKeystore(IntputStream, String)`
        * Use `Keystore.fromStream()` instead.
    * Removed `PrivateKey writeKeystore(OutStream, String)`
        * Use `Keystore.export(OutputStream, String)` instead.
    * Removed `Keystore toKeystore()`
        * Use `new Keystore(PrivateKey)` followed by `Keystore.export(OutputStream, String)` instead.

### Removed `ThresholdKey`
    * Use `KeyList.withThreshold()` or `KeyList.setThreshold()` instead

### Renamed `PublicKey` -> `Key`
    * Addeded by `PublicKey`
    * Addeded by `PrivateKey`
    * Addeded by `KeyList`
    * Addeded by `ContractId`

### `KeyList`
    * Exposed `int threshold`
    * Added `KeyList of(Key...)`
    * Added `KeyList withThreshold(int)`
    * Added `int getThreshold()`
    * Removed `Key toProtoKey()`
    * Removed `SignatureCase getSignatureCase()`
    * Removed `byte[] toBytes()`

### `Mnemonic`
    * Exposed `boolean isLegacy`
    * Added `Mnemonic.fromWords(List<? extends CharSequence>)`
    * Added `Mnemonic generate12()`
    * Added `Mnemoinc generate24()`
    * Removed `Mnemonic generate()`
        * Use `generate12()` or `generate24()` instead.
    * Removed `byte[] toSeed()`
    * Removed `Mnemonic(List<? extends CharSequence>)`
        * Use `Mnemonic.fromWords(List<? extends CharSequence>)` instead.

### Renamed `MnemonicValidationResult` -> `BadMnemonicException`
    * Added `Mnemonic mnemonic`
    * Added `BadMnemonicReason reason`
    * Removed `boolean isOk()`
    * Removed `String toString()`
    * Removed `MnemonicValidationStatus status`

### Renamed `MnemonicValidationStatus` -> `BadMnemonicReason`

### Removed `MirrorClient`
    * Use `Client` instead, and set the mirror network using `setMirrorNetwork()`

### Renamed `MirrorSubscriptionHandle` -> `SubscriptionHandle`

### Renamed `QueryBuilder` -> `Query`
    * Changed `long getCost(Client)` -> `Hbar getCost(Client)`
    * Removed `setPaymentTransaction()`
    * Removed `setQueryPayment(long)`
    * Removed `setMaxQueryPayment(long)`
    * Removed `Query toProto()`

### Combined `TransactionBuilder` and `Transaction`
    * Added `Transaction fromBytes(byte[])`
    * Added `byte[] toBytes()`
    * Added `TransactionId getTransactionId()`
    * Added `Hbar getMaxTransactionFee()`
    * Added `String getTransactionMemo()`
    * Added `Map<AccountId, byte[]> getTransactionHashPerNode()`
    * Added `Duration getTransactionValidDuration()`
    * Added `Transaction signWithOpeator(Client)`
    * Added `Transaction addSignature(PublicKey, byte[])`
    * Added `Map<AccountId, Map<PublicKey, byte[]>> getSignatures()`
    * Renamed `Transaction build(null)` -> `Transaction freeze()`
    * Renamed `Transaction build(Client)` -> `Transaction freezeWith(Client)`
    * Removed `setMaxQueryPayment(long)`
    * Renamed `setNodeId(AccountId)` -> `setNodeAccountIds(List<AccountId>)`

### `AccountBalanceQuery` extends [Query](#renamed-querybuilder-query)
    * Added `AccountId getAccountId()`
    * Added `ContractId getContractId()`
    * Changed `Hbar execute(Client)` -> `AccountBalance execute(Client)`

### Added `AccountBalance`
    * Added `Hbar balance`
    * Added `Map<TokenId, long> tokenBalances`

### `AccountCreateTransaction` extends [Transaction](#combined-transactionbuilder-and-transaction)
    * Added `Key getKey()`
    * Added `Hbar getInitialBalance()`
    * Added `boolean getReceiverSignatureRequired()`
    * Added `AccountId getProxyAccountId()`
    * Added `Duration getAutoRenewPeriod()`
    * Removed `setSendRecordThreshold(long)` and `setSendRecordThreshold(Hbar)`
    * Removed `setReceiveRecordThreshold(long)` and `setReceiveRecordThreshold(Hbar)`

### `AccountDeleteTransaction` extends [Transaction](#combined-transactionbuilder-and-transaction)
    * Added `AccountId getAccountId()`
    * Added `AccountId getTransferAccountId()`
    * Renamed `setDeleteAccountId()` -> `setAccountId()`

### `AccountId`
    * Added `byte[] toBytes()`
    * Added `AccountId fromBytes(byte[])`
    * Renamed `long account` -> `long num`
    * Removed `AccountId(AccountIDOrBuilder)`
    * Removed `AccountId toProto()`

### `AccountInfo`
    * Added `byte[] toBytes()`
    * Added `AccountInfo fromBytes(byte[])`
    * Added `List<LiveHash> liveHashes`
    * Changed `long balance` -> `Hbar balance`
    * Renamed `generateSendRecordThreshold` -> `sendRecordThreshold`
    * Renamed `generateReceiveRecordThreshold` -> `receiveRecordThreshold`

### `AccountInfoQuery` extends [Query](#renamed-querybuilder-query)
    * Added `AccountId getAccountId()`

### `AccountRecordsQuery` extends [Query](#renamed-querybuilder-query)
    * Added `AccountId getAccountId()`

### `AccountStakersQuery` extends [Query](#renamed-querybuilder-query)
    * Added `AccountId getAccountId()`

### `AccountUpdateTransaction` extends [Transaction](#combined-transactionbuilder-and-transaction)
    * Added `AccountId getAccountId()`
    * Added `Key getKey()`
    * Added `Hbar getInitialBalance()`
    * Added `boolean getReceiverSignatureRequired()`
    * Added `AccountId getProxyAccountId()`
    * Added `Duration getAutoRenewPeriod()`
    * Added `Instant getExpirationTime()`
    * Removed `setSendRecordThreshold(long)` and `setSendRecordThreshold(Hbar)`
    * Removed `setReceiveRecordThreshold(long)` and `setReceiveRecordThreshold(Hbar)`

### Removed `CryptoTransferTranscation`
    * Use `TransferTransaction` instead.

### `TransferTransaction` extends [Transaction](#combined-transactionbuilder-and-transaction)
    * Added `TransferTransaction addTokenTransfer(TokenId, AccountId, long)`
    * Added `Map<TokenId, Map<AccountId, Long>> getTokenTransfers()`
    * Added `TransferTransaction addHbarTransfer(AccountId, Hbar)`
    * Added `Map<AccountId, Hbar> getHbarTransfers()`

### Renamed `ContractBytecodeQuery` -> `ContractByteCodeQuery` extends [Query](#renamed-querybuilder-query)
    * Added `ContractId getContractId()`

### `ContractCallQuery` extends [Query](#renamed-querybuilder-query)
    * Added `ContractId getContractId()`
    * Added `long getGas()`
    * Added `byte[] getFunctionParameters()`

### `ContractCreateTransaction` extends [Transaction](#combined-transactionbuilder-and-transaction)
    * Added `FileId getBytecodeFileId()`
    * Added `Key getAdminKey()`
    * Added `long getGas()`
    * Added `Hbar getInitialBalance()`
    * Added `Duration getAutoRenewDuration()`
    * Added `AccountId getProxyAccountId()`
    * Added `String getContractMemo()`
    * Added `byte[] getConstructorParameters()`
    * Removed `setInitialBalance(long)`

### `ContractDeleteTransaction` extends [Transaction](#combined-transactionbuilder-and-transaction)
    * Added `ContractId getContractId()`
    * Added `AccountId getTransferAccountId()`
    * Added `ContractId getTransferContractId()`

### `ContractExecuteTransaction` extends [Transaction](#combined-transactionbuilder-and-transaction)
    * Added `ContractId getContractId()`
    * Added `long getGas()`
    * Added `Hbar getPayableAmount()`
    * Added `byte[] getFunctionParameters()`
    * Removed `setPayableAmount(long)`

### `ContractId`
    * Added `byte[] toBytes()`
    * Added `ContractId fromBytes(byte[])`
    * Renamed `long contract` -> `long num`
    * Removed `ContractId(ContractIDOrBuilder)`
    * Removed `ContractId toProto()`
    * Removed `SignatureCase getSignatureCase()`
    * Removed `Key toProtoKey()`

### `ContractInfo`
    * Added `byte[] toBytes()`
    * Added `ContractInfo fromBytes(byte[])`

### `ContractInfoQuery` extends [Query](#renamed-querybuilder-query)
    * Added `ContractId getContractId()`
    * Removed `Method getMethod()`

### Removed `ContractRecordsQuery`

### `ContractUpdateTransaction` extends [Transaction](#combined-transactionbuilder-and-transaction)
    * Added `ContractId getContractId()`
    * Added `FileId getBytecodeFileId()`
    * Added `Key getAdminKey()`
    * Added `Duration getAutoRenewDuration()`
    * Added `AccountId getProxyAccountId()`
    * Added `String getContractMemo()`
    * Added `Instant getExpirationTime()`

### `FileAppendTransaction`
    * Added `FileId getFileId()`
    * Added `byte[] getContents()`

### `FileContentsQuery`
    * Added `FileId getFileId()`

### `FileCreateTransaction`
    * Added `byte[] getContents()`
    * Added `Collection<Key> getKeys()`
    * Added `Instant getExpirationTime()`
    * Renamed `addKey(Key)` -> `setKeys(Key...)`

### `FileDeleteTransaction`
    * Added `FileId getFileId()`

### `FileId`
    * Added `byte[] toBytes()`
    * Added `FileId fromBytes(byte[])`
    * Renamed `long file` -> `long num`
    * Removed `FileId fromSolidityAddress()`
    * Removed `FileId(FileIDOrBuilder)`
    * Removed `FileId toProto()`
    * Removed `String toSolidityAddress()`

### `FileInfo`
    * Added `byte[] toBytes()`
    * Added `FileInfo fromBytes(byte[])`
    * Update `List<PublicKey> keys` -> `KeyList keys`

### `FileInfoQuery`
    * Added `FileId getFileId()`

### `FileUpdateTransaction`
    * Added `FileId getFileId()`
    * Added `byte[] getContents()`
    * Added `Collection<Key> getKeys()`
    * Added `Instant getExpirationTime()`
    * Renamed `addKey(Key)` -> `setKeys(Key...)`

### Removed `ConsensusClient`
    * Use `Client` instead, and set mirror network using `Client.setMirrorNetwork()`

### Removed `ConsensusToicMessage`

### Renamed  `MirrorConsensusTopicResponse` -> `TopicMessage`
    * Added `TopicMessageChunk[] chunks`
        * This will be non null for a topic message which is constructed from multiple transactions.
    * Renamed `byte[] message` -> `byte[] contents`
    * Removed `byte[] getMessage()`
    * Removed `ConsensusTopicId topicId`

### Renamed `MirrorConsensusTopicChunk` -> `TopicMessageChunk`

### Renamed `MirrorTopicMessageQuery` -> `TopicMessageQuery`
    * Added `setErrorHandler(BiConsumer<Throwable, TopicMessage>)`
        * This error handler will be called if the max retry count is exceeded, or
        * if the subscribe callback errors out for a specific `TopicMessage`
    * Changed `MirrorSubscriptionHandle subscribe(MirrorClient, Consumer<MirrorConsensusTopicResponse>, Consumer<Throwable>)` -> `subscribe(Client, Consumer<TopicMessage>)`
        * Use `setErrorHandler()` instead of passing it in as the third parameter.

### Renamed `ConsensusTopicCreateTransaction` -> `TopicCreateTransaction`
    * Added `String getTopicMemo()`
    * Added `Key getAdminKey()`
    * Added `Key getSubmitKey()`
    * Added `Duration getAutoRenewDuration()`
    * Added `AccountId getAutoRenewAccountId()`

### Renamed `ConsensusTopicDeleteTransaction` -> `TopicDeleteTransaction`
    * Added `TopicId getTopicId()`

### Renamed `ConsensusMessageSubmitTransaction` -> `TopicMessageSubmitTransaction`
    * Added `TopicId getTopicId()`
    * Added `byte[] getMessage()`
    * Removed `setChunkInfo(TransactionId, int, int)`

### Renamed `ConsensusTopicId` -> `TopicId`
    * Renamed `long topic` -> `long num`
    * Removed `ConsensusTopicId(TopicIDOrBuilder)`

### Renamed `ConsensusTopicInfo` -> `TopicInfo`
    * Added `byte[] toBytes()`
    * Added `TopicInfo fromBytes()`
    * Renamed `ConsensusTopicId id` -> `TopicId topicId`

### Renamed `ConsensusTopicInfoQuery` -> `TopicInfoQuery`
    * Added `TopicId getTopicId()`

### Renamed `ConsensusTopicUpdateTransaction` -> `TopicUpdateTransaction`
    * Added `TopicId getTopicId()`
    * Added `String getTopicMemo()`
    * Added `Key getAdminKey()`
    * Added `Key getSubmitKey()`
    * Added `Duration getAutoRenewDuration()`
    * Added `AccountId getAutoRenewAccountId()`

### `TokenAssociateTransaction` extends [Transaction](#combined-transactionbuilder-and-transaction)
    * Added `AccountId getAccountId()`
    * Added `List<TokenId> getTokenIds()`
    * Renamed `addTokenId(TokenId)` -> `setTokenIds(List<TokenId>)`

### Removed `TokenBalanceQuery`
    * Use `AccountBalanceQuery` to fetch token balances since `AccountBalance` contains `tokenBalances`.

### `TokenBurnTransaction` extends [Transaction](#combined-transactionbuilder-and-transaction)
    * Added `TokenId getTokenId()`
    * Added `long getAmount()`

### `TokenCreateTransaction` extends [Transaction](#combined-transactionbuilder-and-transaction)
    * Added `AccountID getTreasuryAccountId()`
    * Added `Key getAdminKey()`
    * Added `Key getKycKey()`
    * Added `Key getSupplyKey()`
    * Added `Key getWipeKey()`
    * Added `Key getFreezeKey()`
    * Added `boolean getFreezeDefault()`
    * Added `Instant getExpirationTime()`
    * Added `AccountId getAutoRenewAccountId()`
    * Added `Duration getAutoRenewPeriod()`
    * Added `int getDecimals()`
    * Renamed `setName(String)` ->`setTokenName(String)`
    * Renamed `setSymbol(String)` ->`setTokenSymbol(String)`
    * Renamed `setTreasury(AccountId)` ->`setTreasuryAccountId(AccountId)`
    * Renamed `setAutoRenewAccount(AccountId)` ->`setAutoRenewAccountId(AccountId)`

### `TokenDeleteTransaction` extends [Transaction](#combined-transactionbuilder-and-transaction)
    * Added `TokenId getTokenId()`

### `TokenDisassociateTransaction` extends [Transaction](#combined-transactionbuilder-and-transaction)
    * Added `AccountId getAccountId()`
    * Added `List<TokenId> getTokenIds()`
    * Renamed `addTokenId(TokenId)` -> `setTokenIds(List<TokenId>)`

### `TokenFreezeTransaction` extends [Transaction](#combined-transactionbuilder-and-transaction)
    * Added `TokenId getTokenId()`
    * Added `AccountId getAccointId()`

### `TokenGrantKycTransaction` extends [Transaction](#combined-transactionbuilder-and-transaction)
    * Added `TokenId getTokenId()`
    * Added `AccountId getAccointId()`

### `TokenId`
    * Added `byte[] toBytes()`
    * Added `TokenId romBytes(byte[])`
    * Removed `TokenId(TokenIDOrBuilder)`
    * Removed `fromSolidityAddress(String)`
    * Removed `String toSolidityAddress()`
    * Removed `TokenId toProto()`

### `TokenInfo`
    * Added `byte[] toBytes()`
    * Added `TokenInfo romBytes(byte[])`
    * Renamed `AccountId treasury` -> `AccountId treasuryAccountId`
    * Renamed `Instant expiry` -> `Instant expirationTime`

### `TokenInfoQuery` extends [Query](#renamed-querybuilder-query)
    * Added `TokenId getTokenId()`

### `TokenMintTransaction` extends [Transaction](#combined-transactionbuilder-and-transaction)
    * Added `TokenId getTokenId()`
    * Added `long getAmount()`

### `TokenRelationship`
    * Added `byte[] toBytes()`
    * Added `TokenRelationship fromBytes(byte[])`

### `TokenRevokeKycTransaction` extends [Transaction](#combined-transactionbuilder-and-transaction)
    * Added `TokenId getTokenId()`
    * Added `AccountId getAccointId()`

### Removed `TokenTransferTransaction`
    * Use `TransferTransaction` instead.

### `TokenUnfreezeTransaction` extends [Transaction](#combined-transactionbuilder-and-transaction)
    * Added `TokenId getTokenId()`
    * Added `AccountId getAccointId()`

### `TokenUpdateTransaction` extends [Transaction](#combined-transactionbuilder-and-transaction)
    * Added `AccountID getTreasuryAccountId()`
    * Added `Key getAdminKey()`
    * Added `Key getKycKey()`
    * Added `Key getSupplyKey()`
    * Added `Key getWipeKey()`
    * Added `Key getFreezeKey()`
    * Added `boolean getFreezeDefault()`
    * Added `Instant getExpirationTime()`
    * Added `AccountId getAutoRenewAccountId()`
    * Added `Duration getAutoRenewPeriod()`
    * Added `int getDecimals()`
    * Renamed `setName(String)` ->`setTokenName(String)`
    * Renamed `setSymbol(String)` ->`setTokenSymbol(String)`
    * Renamed `setTreasury(AccountId)` ->`setTreasuryAccountId(AccountId)`
    * Renamed `setAutoRenewAccount(AccountId)` ->`setAutoRenewAccountId(AccountId)`

### `TokenWipeTransaction` extends [Transaction](#combined-transactionbuilder-and-transaction)
    * Added `TokenId getTokenId()`
    * Added `AccountId getAccountId()`

### `FreezeTransaction`
    * Added `Instant getStartTime()`
    * Added `Instant getEndTime()`

### Removed `HbarRangeException`
    * If `Hbar` is out of range `Hedera` will error instead.

### Removed `HederaConstants`
    * No replacement.

### Removed `HederaNetworkException`

### Renamed `HederaPrecheckStatusException` -> `PrecheckStatusException`

### Renamed `HederaReceiptStatusException` -> `ReceiptStatusException`

### Removed `HederaRecordStatusException`
    * `ReceiptStatusException` will be thrown instead.

### Removed`HederaStatusException`
    * A `PrecheckStatusException` or `ReceiptStatusException` will be thrown instead.

### Removed `HederaThrowable`
    * No replacement.

### Removed `LocalValidationException`
    * No replacement. Local validation is no longer done.

### `SystemDeleteTransaction`
    * Added `FileId getFileId()`
    * Added `ContractId getContractId()`
    * Added `Instant getExpirationTime()`

### `SystemUndeleteTransaction`
    * Added `FileId getFileId()`
    * Added `ContractId getContractId()`

### `TransactionId`
    * Added `byte[] toBytes()`
    * Added `TransactionId fromBytes(byte[])`
    * Removed `TransactionId(TransactionIDOrBuilder)`
    * Removed `TransactionID toProto()`
    * Removed `TransactionId withValidStart(AccountId, Instant)`
        * Use `new TransactionId(AccountId, Instant)` instead.
    * Removed `TransactionId(AccountId)`
        * Use `TransactionId generate(AccountId)` instead.

### Removed `TransactionList`

### `TransactionReciept`
    * Exposed `ExchangeRate exchangeRate`
    * Exposed `AccountId accountId`
    * Exposed `FileId fileId`
    * Exposed `ContractId contractId`
    * Exposed `TopicId topicId`
    * Exposed `TokenId tokenId`
    * Exposed `long topicSequenceNumber`
    * Exposed `byte[] topicRunningHash`
    * Added `byte[] toBytes()`
    * Added `TransactionReceipt fromBytes()`
    * Added `long totalSupply`
    * Removed `AccountId getAccountId()`
        * Use `AccountId accountId` directly instead.
    * Removed `ContractId getContractId()`
        * Use `ContractId contractId` directly instead.
    * Removed `FileId getFileId()`
        * Use `FileId fileId` directly instead.
    * Removed `TokenId getTokenId()`
        * Use `TokenId tokenId` directly instead.
    * Removed `ConsensusTopicId getConsensusTopicId()`
        * Use `TopicId topicId` directly instead.
    * Removed `long getConsensusTopicSequenceNumber()`
        * Use `long sequenceNumber` directly instead.
    * Removed `byte[] getConsnsusTopicRunningHash()`
        * Use `byte[] topicRunningHash` directly instead.
    * Removed `TransactionReceipt toProto()`

### `TransactionReceiptQuery` extends [Query](#renamed-querybuilder-query)
    * Added `TransactionId getTransactionId()`

### `TransactionRecord`
    * Added `byte[] toBytes()`
    * Added `TransactionRecord fromBytes()`
    * Removed `ContractFunctionResult getContratcExecuteResult()`
        * Use `ContractFunctionResult contractFunctionResult` directly instead.
    * Removed `ContractFunctionResult getContratcCreateResult()`
        * Use `ContractFunctionResult contractFunctionResult` directly instead.
    * Removed `TransactionRecord toProto()`

### `TransactionRecordQuery` extends [Query](#renamed-querybuilder-query)
    * Added `TransactionId getTransactionId()`

### `Hbar`
    * Added `Hbar fromString(CharSequence)`
    * Added `Hbar fromString(CharSequence, HbarUnit)`
    * Added `Hbar from(long, HbarUnit)`
    * Added `Hbar from(BigDecimal, HbarUnit)`
    * Added `BigDecimal getValue()`
    * Added `String toString(HbarUnit)`
    * Renamed `fromTinybar(long)` -> `fromTinybars(long)`
    * Renamed `Hbar of(long)` -> `from(long)`
    * Renamed `Hbar of(BigDecimal)` -> `from(BigDecimal)`
    * Renamed `Hbar as(HbarUnit)` -> `to(HbarUnit)`
    * Renamed `long asTinybar()` -> `long toTinybars()`

### `Client`
    * Added `void setMirrorNetwork(List<String>)`
    * Added `List<String> getMirrorNetwork()`
    * Added `Client forNetwork(Map<AccountId, String>)`
    * Added `void ping(AccountId)`
    * Added `PublicKey getOperatorPublicKey()`
    * Added `Client setNetwork(Map<String, AccountId>)`
    * Added `Map<String, AccountId> getNetwork()`
    * Renamed `fromJson(String)` -> `fromConfig(String)` and `fromJson(Reader)` -> `fromConfig(Reader)`
    * Renamed `fromFile(String)` -> `fromConfigFile(String)` and `fromFile(File)` -> `fromConfigFile(File)`
    * Renamed `getOperatorId()` -> `getOperatorAccountId()`
    * Removed `constructor(Map<AccountId, String>)`
    * Removed `Client replaceNodes(Map<AccountId, String>)`
    * Removed `Client setMaxTransactionFee(long)`
    * Removed `Client setMaxQueryPayment(long)`
    * Removed `AccountInfo getAccount(AccountId)`
    * Removed `void getAccountAsync()`
    * Removed `Hbar getAccountBalance(AccountId)`
        * Use `AccountBalanceQuery` instead.
    * Removed `void getAccountBalanceAsync()`
    * Changed `Client setOperatorWith(AccountId, PublicKey, TransactionSigner)`-> `Client setOperatorWith(AccountId, PublicKey, Function<bytes, bytes>)`

## v2.0.1

### Bug Fixes

#### `TokenCreateTransaction`

 * `long getAutoRenewPeriod()` -> `Duration getAutoRenewPeriod()`
 * `setAutoRenewPeriod(long)` -> `setAutoRenewPeriod(Duration)`
 * `long getExpirationTime()` -> `Instant getExpirationTime()`
 * `setExpirationTime(long)` -> `setExpirationTime(Instant)`

#### `TokenUpdateTransaction`
 * `long getAutoRenewPeriod()` -> `Duration getAutoRenewPeriod()`
 * `setAutoRenewPeriod(long)` -> `setAutoRenewPeriod(Duration)`
 * `long getExpirationTime()` -> `Instant getExpirationTime()`
 * `setExpirationTime(long)` -> `setExpirationTime(Instant)`

#### `TokenInfo`
 * `AccountId treasury()` -> `AccountId treasuryAccountId()`
 * `long autoRenewPeriod()` -> `Duration autoRenewPeriod()`
 * `long expirationTime()` -> `Instant expirationTime()`

## v2.0.0

### General changes

  * No longer support the use of `long` for `Hbar` parameters. Meaning you can no longer do
    `AccountCreateTransaction().setInitialBalance(5)` and instead **must**
    `AccountCreateTransaction().setInitialBalance(new Hbar(5))`. This of course applies to more than just
    `setInitialBalance()`.
  * Any method that used to require a `PublicKey` will now require `Key`.
    * `AccountCreateTransaction.setKey(PublicKey)` is now `AccountCreateTransaction.setKey(Key)` as an example.
  * All `Id` types (`Account`, `File`, `Contract`, `Topic`, and `TransactionId`)
    * Support `fromBytes()` and `toBytes()`
    * No longer have the `toProto()` method.
  * The use of `Duration` in the SDK will be either `java.time.Duration` or `org.threeten.bp.Duration` depending
    on which JDK and platform you're developing on.
  * The use of `Instant` in the SDK will be either `java.time.Instant` or `org.threeten.bp.Instant` depending
    on which JDK and platform you're developing on.
  * All transactions and queries will now attempt to execute on more than one node.
  * More `getCostAsync` and `executeAsync` variants
    * `void executeAsync(Client)`
    * `Future executeAsync(Client, BiConsumer<O, T>)`
    * `void executeAsync(Client, Duration timeout, BiConsumer<O, T>)`
    * `void getCostAsync(Client)`
    * `Future getCostAsync(Client, BiConsumer<O, T>)`
    * `void getCostAsync(Client, Duration timeout, BiConsumer<O, T>)`
  * Building different types from a protobuf type is no longer supported. Use `fromBytes` instead.
  * `getSignatureCase()` is no longer accessible
  * Field which were `byte[]` are now `ByteString` to prevent extra copy operation. This includes
    the response type of `FileContentsQuery`

### Renamed Classes

  * `ConsensusSubmitMessageTransaction` -> `MessageSubmitTransaction`
  * `ConsensusTopicCreateTransaction` -> `TopicCreateTransaction`
  * `ConsensusTopicDeleteTransaction` -> `TopicDeleteTransaction`
  * `ConsensusTopicUpdateTransaction` -> `TopicUpdateTransaction`
  * `ConsensusTopicId` -> `TopicId`
  * `Ed25519PublicKey` -> `PublicKey`
  * `Ed25519PrivateKey` -> `PrivateKey`

### Removed Classes

  * `HederaNetworkException`
  * `MnemonicValidationResult`
  * `HederaConstants`
  * `ThresholdKey` use `KeyList.withThreshold()` instead.

### New Classes

  * LiveHash: Support for Hedera LiveHashes
  * Key: A common base for the signing authority or key entities in Hedera may have.

### Client

#### Renamed

  * `Client()` -> `Client.forNetwork()`
  * `Client.fromFile()` -> `Client.fromJsonFile()`
  * `Client.replaceNodes()` -> `Client.setNetwork()`

### PrivateKey

#### Changes

  * `sign()` no longer requires offset or length parameters

#### Removed

  * `writePem()`

### PublicKey

#### Added

  * `verify()` verifies the message was signed by public key

### Hbar

#### Renamed

  * `as()` -> `to()`
  * `asTinybar()` -> `toTinybars()`
  * `fromTinybar()` -> `fromTinybars()`

#### Added

  * `negated()`
  * `getValue()`

#### Removed

  * `Hbar(BigDecimal, HbarUnit)`
  * `Hbar.from(long)`
  * `Hbar.from(BigDecimal)`
  * `Hbar.of()`

### KeyList

#### Added

  * `KeyList.withThreshold()`
  * `size()`
  * `isEmpty()`
  * `contains()`
  * `containsAll()`
  * `iterator()`
  * `toArray()`
  * `remove()`
  * `removeAll()`
  * `retainAll()`
  * `clear()`
  * `toString()`

### Mnemonic

#### Renamed

  * `Mnemonic(List<? extends CharSequence>)` -> `Mnemonic.fromWords() throws BadMnemonicException`

### ContractId

#### Added

  * `ContractId(long)`

#### Removed

  * `toKeyProto()`
  * `implements PublicKey` meaning it can no longer be used in place of a Key

### FileId

#### Added

  * `FileId(long)`

#### Removed

  * `fromSolidityAddress()`
  * `toSolidityAddress()`

### TransactionId

#### Added

  * `TransactionId.withValidStart()`
  * `TransactionId.generate()`

### Transaction

#### Added
  * `Transaction.hash()`
  * `Transaction.signWithOperator()`

#### Removed

  * `getReceipt()`
  * `getRecord()`

### QueryBuilder

#### Removed

  * `toProto()`
  * `setPaymentTransaction()`

### TransactionBuilder

### AccountInfo

#### Added

  * `List<LiveHash> liveHashes`

### AccountDeleteTransaction

#### Renamed

  * `setDeleteAccountId()` -> `setAccountId()`
  * Removed `addKey()`, use `setKeys(Key...)` instead.

### FileUpdateTransaction

  * Removed `addKey()`, use `setKeys(Key...)` instead.

## v1.1.4

### Added

 * Support for loading Ed25519 keys from encrypted PEM files (generated from OpenSSL).

 * Add a method to validate a mnemonic word list for accurate entry.

### Fixed

 * Fixed `TransactionReceiptQuery` not waiting for consensus some times.

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

