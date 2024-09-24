/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2022 - 2024 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

repositories {
    google()
    mavenCentral()
}

android {
    compileSdk = 34
    namespace = "com.hedera.android_example"

    defaultConfig {
        applicationId = "com.hedera.android_example"
        minSdk = 26
        targetSdk =  34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles.add(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    packaging {
        resources.excludes.add("META-INF/versions/9/OSGI-INF/MANIFEST.MF")
    }
}

dependencies {
    // --- Remove to use a published SDK version ---
    implementation(platform("com.hedera.hashgraph:sdk-dependency-versions"))
    // ---------------------------------------------

    implementation("com.hedera.hashgraph:sdk:2.40.0")

    implementation("com.google.android.material:material:1.11.0")

    implementation("androidx.appcompat:appcompat")
    implementation("androidx.constraintlayout:constraintlayout")
    implementation("androidx.coordinatorlayout:coordinatorlayout")
    implementation("androidx.fragment:fragment")
    implementation("androidx.lifecycle:lifecycle-common")
    implementation("androidx.lifecycle:lifecycle-viewmodel")
    implementation("androidx.recyclerview:recyclerview")
    implementation("androidx.viewpager2:viewpager2")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    runtimeOnly("io.grpc:grpc-okhttp")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.12")
}
