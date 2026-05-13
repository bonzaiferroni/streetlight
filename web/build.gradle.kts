@file:OptIn(ExperimentalDistributionDsl::class, ExperimentalWasmDsl::class)

import org.gradle.api.tasks.Copy
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
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
    wasmJs {
        binaries.executable()
        browser()
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.serialization.cbor)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.html)
            implementation(kotlinWrappers.css)
            implementation(project(":model"))
            implementation(project(":koala"))
            implementation(project(":kabinet"))

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
        }

        jsMain.dependencies {
            implementation(libs.kotlinx.html.js)
            // implementation(npm("@js-joda/timezone", "2.23.0"))
        }

        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
    }
}
