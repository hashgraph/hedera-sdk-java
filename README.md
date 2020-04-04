![](https://img.shields.io/badge/java-8%2B-blue)
![](https://img.shields.io/badge/android-19%2B-blue)

# Hedera™ Hashgraph Java SDK

> The Java SDK for interacting with Hedera Hashgraph: the official distributed
> consensus platform built using the hashgraph consensus algorithm for fast,
> fair and secure transactions. Hedera enables and empowers developers to
> build an entirely new class of decentralized applications.

## ⚠️ Disclaimer

This project is currently under a re-development effort. The goals are as follows:

 * Native support for Android

 * Native support for the Corda DJVM

 * Usage of modern Java techniques (such as futures) to reduce internal control flow complexities

 * End-to-end test coverage

Join the [Hedera discord](https://hedera.com/discord) for the latest updates and announcements.

## Development

### Dependencies

 * [Java Development Kit (JDK)](https://adoptopenjdk.net/) v8+

### Compile

```sh
$ ./gradlew compileJava
```

### Test

```sh
$ ./gradlew test
```

## Contributing to this Project

We welcome participation from all developers!
For instructions on how to contribute to this repo, please
review the [Contributing Guide](CONTRIBUTING.md).

## License Information

Licensed under Apache License,
Version 2.0 – see [LICENSE](LICENSE) in this repo
or [apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0).
