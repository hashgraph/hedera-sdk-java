# Example Android app

Running the example requires `operator_id` and `operator_key` resource strings to be set in `app/src/main/res/values/strings.xml`

```xml
<string name="operator_id">...</string>
<string name="operator_key">...</string>
```

## Running with a local SDK version

The example uses the local SDK version. If you want to use the example stand-alone with an SDK version published to
Maven Central you need to remove the line `includeBuild("..")` from [settings.gradle.kts](settings.gradle.kts).
