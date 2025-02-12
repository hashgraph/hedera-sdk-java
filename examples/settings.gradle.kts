// SPDX-License-Identifier: Apache-2.0
plugins { id("org.hiero.gradle.build") version "0.3.3" }

@Suppress("UnstableApiUsage") dependencyResolutionManagement { repositories.mavenCentral() }

// --- Remove to use a published SDK version ---
includeBuild("..")
