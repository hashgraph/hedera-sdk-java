![](https://img.shields.io/badge/java-8%2B-blue)
![](https://img.shields.io/badge/android-19%2B-blue)

# Hedera™ Hashgraph Java SDK

> The Java SDK for interacting with Hedera Hashgraph: the official distributed
> consensus platform built using the hashgraph consensus algorithm for fast,
> fair and secure transactions. Hedera enables and empowers developers to
> build an entirely new class of decentralized applications.

## ⚠️ Disclaimer

This project is currently under a re-development effort. The goals are as follows:

 * Native support for Android 19+

 * Native support for the Corda DJVM

 * Usage of modern Java techniques (such as futures) to reduce internal control flow complexities

 * End-to-end test coverage

 * Minimal breaking changes

Note that at the end of the effort the changes will be squashed to a single commit  on top of `master`
to preserve development history.

If you are trying this out and notice something missing from master, feel free to open an issue. It's likely
it was simply overlooked.

Join the [Hedera discord](https://hedera.com/discord) for the latest updates and announcements.

## Development

### Dependencies

 * [Java Development Kit (JDK)](https://adoptopenjdk.net/) v12+ (note this is to _build_, not run)

### Compile

```sh
$ ./gradlew compileJava
```

### Unit Test

```sh
$ ./gradlew test
```

### Integration Test

Requires `OPERATOR_ID` and `OPERATOR_KEY` to be in the environment. Integration tests run against
the Hedera test network.

```sh
$ export OEPRATOR_ID="..."
$ export OPERATOR_KEY="..."

$ ./gradlew integrationTest
```

### Example

Requires `OPERATOR_ID` and `OPERATOR_KEY` to be in the environment. Integration tests run against
the Hedera test network.

```sh
$ export OEPRATOR_ID="..."
$ export OPERATOR_KEY="..."

$ ./gradlew -q example:run<NameOfExample>
$ ./gradlew -q example:runGenerateKey
```

## Contributing to this Project

We welcome participation from all developers!
For instructions on how to contribute to this repo, please
review the [Contributing Guide](CONTRIBUTING.md).

## License Information

Licensed under Apache License,
Version 2.0 – see [LICENSE](LICENSE) in this repo
or [apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0).
