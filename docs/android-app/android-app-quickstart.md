## Get started

> Please note that the minimal Android SDK level required for using the Hedera SDK in an Android project is **26**.

To get started with an Android project, you'll need to add the following two dependencies:

1. Hedera Java SDK:
```groovy
implementation 'com.hedera.hashgraph:sdk:2.29.0'
```

2. gRPC implementation:
```groovy
// okhttp transport (for lighter-weight applications or Android)
implementation 'io.grpc:grpc-okhttp:1.58.0'
```

## Next steps
To make it easier to start your Android project using the Hedera Java SDK,
we recommend checking out the [Android example](../../example-android/README.md).
This examples show different uses and workflows,
giving you valuable insights into how you can use the Hedera platform in your Android projects.
