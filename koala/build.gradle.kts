@file:OptIn(ExperimentalDistributionDsl::class)

import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalDistributionDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    kotlin("plugin.js-plain-objects") version "2.3.10"
}

kotlin {
    jvm()
    js(IR) {
        browser {
            commonWebpackConfig {
                sourceMaps = true
            }
            distribution {
                outputDirectory.set(projectDir.resolve("../www/js/koala"))
            }
        }
        binaries.executable()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.html)
                implementation(kotlinWrappers.css)
                api(project(":kampfire"))
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(libs.kotlinx.html.js)
            }
        }
    }
}
