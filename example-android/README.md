# Example Android app

Running the example requires `operator_id` and `operator_key` resource strings to be set in `app/src/main/res/values/strings.xml`

```xml
<string name="operator_id">...</string>
<string name="operator_key">...</string>
```

## Running with a local SDK version

By default, the example uses the SDK version published on Maven Central. If you want to use the local version you need to do this:

- Publish the SDK to the local Maven repo - `./gradlew publishToMavenLocal`
- Uncomment the `mavenLocal()` line in `example-android/build.gradle`
- Build the example - `./gradlew :example-android:build --refresh-dependencies`
- Run the example
