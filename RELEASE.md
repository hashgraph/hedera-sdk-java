## Release process of Java SDK

### Setup

- Create an account in [sonatype](https://issues.sonatype.org/secure/Signup!default.jspa)
- Someone has to create a JIRA ticket to add your account to the "com.hedera" group ([example](https://issues.sonatype.org/browse/OSSRH-85535))
- Add these properties in your global `gradle.properties` file (`~/.gradle/gradle.properties`):
  - `sonatypeUsername=<your-email>`
  - `sonatypePassword=<your-password>`
  - `signing.keyId=<last-8-characters-of-your-GPG-key`
  - `signing.password=<password-of-the-key>`
  - `signing.secretKeyRingFile=/Users/<user>/.gnupg/secring.gpg`

### Releasing

- Make sure you don’t have any uncommitted changes (The publish task will remove them)
- **Run** the [local node](https://github.com/hashgraph/hedera-local-node)
- Run `task test:integration` !!!
- **Stop** the local node
- Run `task test:unit`
- Create a new branch: `release/vX.Y.Z`
- Change the version in `version.gradle`
- Change the version in `CHANGELOG.md` (from unreleased)
- Change the version in `example-android/app/build.gradle`
- Change the version in README.md (Once in the Gradle section and once in the Maven section)
- Push the changes
- run `task publish`
- Create a PR to merge release/vX.Y.Z to develop
- Finally, merge develop to main
- Go to the [SDK PAGE](https://github.com/hashgraph/hedera-sdk-js/releases) and press the “Draft a new release” button
- Create a new tag from the branch that you are releasing (release/vX.Y.Z) with the same version of the branch
- Copy the latest changes from CHANGELOG.md to the description of the release
- Publish Release
