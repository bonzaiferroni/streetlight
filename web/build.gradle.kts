@file:OptIn(ExperimentalDistributionDsl::class)

import org.gradle.api.tasks.Copy
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalDistributionDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    kotlin("plugin.js-plain-objects") version "2.3.10"
}

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                sourceMaps = true
            }
            distribution {
                outputDirectory.set(projectDir.resolve("../www/js/streetlight"))
            }
        }
        binaries.executable()
    }
    jvm()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.html)
                implementation(project(":model"))
                implementation(project(":koala"))
                implementation(project(":kabinet"))
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(libs.kotlinx.html.js)
                // implementation(npm("@js-joda/timezone", "2.23.0"))
            }
        }
    }
}
