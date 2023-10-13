## JVM
JDK 17 is required. The Temurin builds of [Eclipse Adoptium](https://adoptium.net/) are strongly recommended.

## Setup

### Building

```sh
$ ./gradlew compileJava
```

### Unit Tests

```sh
$ ./gradlew test
```

### Integration Tests

> The tests are only executed if the configuration is provided
> as an environment variable (see the `onlyIf` block in [`sdk/build.gradle`](../../sdk/build.gradle)).
> That's why we need to pass the configuration file at the beginning of the command.
> The `CONFIG_FILE` environment variable is not used, so you can provide any value,
> but it should not be `null`.

#### Using system properties
`OPERATOR_ID`, `OPERATOR_KEY` and `HEDERA_NETWORK` must be passed into system properties.\
`HEDERA_NETWORK` can be set to `localhost`, `testnet` or `previewnet`.

```sh
$ CONFIG_FILE=whatever ./gradlew integrationTest -POPERATOR_ID="<shard.realm.num>" -POPERATOR_KEY="<PrivateKey>" -PHEDERA_NETWORK="<network>"
```

#### Using configuration file

```sh
$ CONFIG_FILE=whatever ./gradlew integrationTest -PCONFIG_FILE="<ConfigurationFilePath>"
```

An example configuration file can be found in the repo here:
[sdk/src/test/resources/client-config-with-operator.json](../../sdk/src/test/resources/client-config-with-operator.json)

The format of the configuration file should be as follows:
```
{
    "network": "testnet",
    "operator": {
        "accountId": "0.0.7",
        "privateKey": "d5d37..."
    }
}
```

`HEDERA_NETWORK` can be set to `testnet`, `previewnet` or `mainnet`.

#### Running individual test classes or functions

Running test class:
```sh
$ CONFIG_FILE=whatever ./gradlew integrationTest -POPERATOR_ID="<shard.realm.num>" -POPERATOR_KEY="<PrivateKey>" -PHEDERA_NETWORK="testnet" --tests "<TestClass>"
```

Running test function:
```sh
$ CONFIG_FILE=whatever ./gradlew integrationTest -POPERATOR_ID="<shard.realm.num>" -POPERATOR_KEY="<PrivateKey>" -PHEDERA_NETWORK="testnet" --tests "<TestClass.functionName>"
```

## Maintaining generated files
>To execute the tasks below, you need to install the tool from this link: https://taskfile.dev/
> (these tasks are from the file Taskfile.yml, which is located in the root of the repository).
> Once installed, you can run the commands as shown below.

### Updating unit tests snapshots
```shell
$ task update:snapshots
```

### Updating proto files
```shell
$ task update:proto
```

### Updating address books
Update all address books:
```shell
$ task update:addressbooks
```
Update address books only for a mainnet:
```shell
$ task update:addressbooks:mainnet
```
Update address books only for a testnet:
```shell
$ task update:addressbooks:testnet
```
Update address books only for a previewnet:
```shell
$ task update:addressbooks:previewnet
```
