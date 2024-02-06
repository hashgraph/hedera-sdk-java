## Java examples

### SDK
* [Construct a client](../examples/src/main/java/ConstructClientExample.java)
* [Generate a key](../examples/src/main/java/GenerateKeyExample.java)
* [Generate a key (with mnemonic)](../examples/src/main/java/GenerateKeyWithMnemonicExample.java)
* [Get address book](../examples/src/main/java/GetAddressBookExample.java)
* [Get exchange rates](../examples/src/main/java/GetExchangeRatesExample.java)
* [Logger](../examples/src/main/java/LoggerFunctionalitiesExample.java)

### Transactions
* [Sign a transaction](../examples/src/main/java/SignTransactionExample.java)

### Schedule Transaction
* [Sign a scheduled transfer transaction](../examples/src/main/java/ScheduleExample.java)
* [Sign a scheduled transfer transaction (with comments)](../examples/src/main/java/ScheduledTransferExample.java)
* [Schedule identical transaction](../examples/src/main/java/ScheduleIdenticalTransactionExample.java)
* [Schedule multisig transaction](../examples/src/main/java/ScheduleMultiSigTransactionExample.java)
* [Schedule multisig transaction (with threshold)](../examples/src/main/java/ScheduledTransactionMultiSigThresholdExample.java)

### Accounts and HBAR
* [Create an account](../examples/src/main/java/CreateAccountExample.java)
* [Create an account (threshold key)](../examples/src/main/java/CreateAccountThresholdKeyExample.java)
* [Create an account (with alias)](../examples/src/main/java/CreateAccountWithAliasExample.java)
* [Create an account (with alias and receiver signature required)](../examples/src/main/java/CreateAccountWithAliasAndReceiverSignatureRequiredExample.java)
* [Account creation ways](../examples/src/main/java/AccountCreationWaysExample.java)
* [Create an account with Hts](../examples/src/main/java/AccountCreateWithHtsExample.java)
* [Auto create an account with transfer transaction](../examples/src/main/java/AutoCreateAccountTransferTransactionExample.java)
* [Account alias](../examples/src/main/java/AccountAliasExample.java)
* [Account allowance](../examples/src/main/java/AccountAllowanceExample.java)
* [Get account info](../examples/src/main/java/GetAccountInfoExample.java)
* [Get account balance](../examples/src/main/java/GetAccountBalanceExample.java)
* [Update an account](../examples/src/main/java/UpdateAccountPublicKeyExample.java)
* [Delete an account](../examples/src/main/java/DeleteAccountExample.java)
* [Staking](../examples/src/main/java/StakingExample.java)
* [Staking (with update)](../examples/src/main/java/StakingWithUpdateExample.java)
* [Multisig](../examples/src/main/java/MultiSigOfflineExample.java)

### Consensus Service
* [Create a topic](../examples/src/main/java/CreateTopicExample.java)
* [Topic management](../examples/src/main/java/TopicWithAdminKeyExample.java)
* [Consensus Pub Sub](../examples/src/main/java/ConsensusPubSubExample.java)
* [Consensus Pub Sub (chunked)](../examples/src/main/java/ConsensusPubSubChunkedExample.java)
* [Consensus Pub Sub (with submit key)](../examples/src/main/java/ConsensusPubSubWithSubmitKeyExample.java)

### Token Service
* [Transfer Hbar](../examples/src/main/java/TransferCryptoExample.java)
* [Transfer Hbar (multi app)](../examples/src/main/java/MultiAppTransferExample.java)
* [Transfer tokens](../examples/src/main/java/TransferTokensExample.java)
* [Transfer using EVM address](../examples/src/main/java/TransferUsingEvmAddressExample.java)
* [Custom fees](../examples/src/main/java/CustomFeesExample.java)
* [Custom fees (exempt)](../examples/src/main/java/ExemptCustomFeesExample.java)
* [NFT Allowances](../examples/src/main/java/NftAddRemoveAllowancesExample.java)
* [Zero token operations](../examples/src/main/java/ZeroTokenOperationsExample.java)

### File Service
* [Create a file](../examples/src/main/java/CreateFileExample.java)
* [File append (chunked)](../examples/src/main/java/FileAppendChunkedExample.java)
* [Get file contents](../examples/src/main/java/GetFileContentsExample.java)
* [Delete a file](../examples/src/main/java/DeleteFileExample.java)

### Smart Contract Service
* [Create a contract](../examples/src/main/java/CreateSimpleContractExample.java)
* [Create a stateful contract](../examples/src/main/java/CreateStatefulContractExample.java)
* [Contract nonce (HIP-729)](../examples/src/main/java/ContractNoncesExample.java)
* [Interaction with a contract](../examples/src/main/java/SolidityPrecompileExample.java)

### Miscellaneous
* [Checksum validation](../examples/src/main/java/ValidateChecksumExample.java)
* [Pseudorandom Number Generator](../examples/src/main/java/PrngExample.java)

## Usage

### Configuration
Running the examples requires `.env` file to exist under [`root project folder`](../):

```sh
$ cp .env.sample ../.env
```

The `OPERATOR_ID` and `OPERATOR_KEY` variables should be set in a `.env` file.
Optionally, you can set the `HEDERA_NETWORK` variable to `testnet`, `previewnet`, or `mainnet`
for configuring the network. If the `HEDERA_NETWORK` is not set, it will default to `testnet`.\
Therefore, the format of the configuration file should be as follows:

```.properties
OPERATOR_ID=0.0.102...
OPERATOR_KEY=0xeae...
# Optionally set HEDERA_NETWORK
HEDERA_NETWORK=previewnet
```

### Running with Gradle

> Note that the below `./gradlew` commands should be run from the root of the project.

Template:

```sh
$ ./gradlew -q example:run<NameOfExample>
```

Concrete example:

```sh
$ ./gradlew -q example:runGenerateKey
```

### Running with Intellij
Simply execute the main function of the desired example.
