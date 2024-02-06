## Migrating from v1 -> v2

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

