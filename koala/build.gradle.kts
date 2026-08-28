@file:OptIn(ExperimentalDistributionDsl::class)
@file:Suppress("OPT_IN_USAGE")

import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalDistributionDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.jsPlainObjects)
}

kotlin {
    jvm()
    js {
        browser {
            commonWebpackConfig {
                sourceMaps = true
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
            distribution {
                outputDirectory.set(projectDir.resolve("../www/js/koala"))
            }
        }
        binaries.executable()
    }
    wasmJs {
        binaries.executable()
        browser()
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.html)
            implementation(kotlinWrappers.css)
            api(project(":kampfire"))

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)

            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        jsMain.dependencies {
            implementation(libs.kotlinx.html.js)
            implementation(kotlinWrappers.browser)
        }

        wasmJsMain.dependencies {
        }
    }

    compilerOptions {
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
    }
}
