#!/bin/bash

echo "Building the example with the local SDK version"
cd example-android || exit

# Check if adb is installed
if ! command -v adb &> /dev/null;
then
    echo "adb could not be found"
    exit 1
fi

# Swap the build files
mv app/build.gradle app/build.gradle.bak
mv app/build-with-local-SDK.gradle app/build.gradle

# Build the app and install it on the emulator
if ../gradlew assembleDebug; then
    ../gradlew installDebug
    adb -s emulator-5554 shell am start -n com.hedera.android_example/.MainActivity
else
    echo 'Build failed'
fi

# Swap back the build files
mv app/build.gradle app/build-with-local-SDK.gradle
mv app/build.gradle.bak app/build.gradle
cd ..
