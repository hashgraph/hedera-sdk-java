# Example Android application

## Description
Example Android Application is designed to showcase various functionalities
and capabilities when working with the Hedera™ Java SDK.
It operates on a `testnet` environment and offers the following features
through three distinct tabs:
- **Private Key tab** allows you to generate ED25519 key pairs by pressing
the "Generate" button.
- **Account Balance tab** allows you to retrieve the balance of an account by
pressing the "Get Account Balance" button and specifying the desired account,
which can be different from the default account specified in the
  [`app/src/main/res/values/strings.xml`](../example-android/app/src/main/res/values/strings.xml) file.
- **Crypto Transfer tab** allows you to transfer a specified amount of HBARs
from your account to a recipient account by pressing the "Send HBAR" button
and setting the recipient's account in the provided text field.

## Usage

### Configuration
Running the example requires `operator_id` and `operator_key`
resource strings to be set in
[`app/src/main/res/values/strings.xml`](../example-android/app/src/main/res/values/strings.xml).

```xml
<string name="operator_id">...</string>
<string name="operator_key">...</string>
```

## Running with a local SDK version

The example uses the local SDK version. If you want to use the example stand-alone with an SDK version published to
Maven Central you need to remove the line `includeBuild("..")` from [settings.gradle.kts](settings.gradle.kts).

> Note that the above `./gradlew` commands should be run from the root of the project.

> If you get the following error:
> `Cannot perform signing task ':sdk:signMavenJavaPublication' because it has no configured signatory`
> , then you should be able to resolve the error by providing the expected GPG variables in
> the `gradle.properties` file in your `HOME` directory:

```properties
# File location: ~/.gradle/gradle.properties - see https://docs.gradle.org/current/userguide/directory_layout.html
signing.keyId=248...
signing.password=sec...
signing.secretKeyRingFile=/Users/me/...
```
