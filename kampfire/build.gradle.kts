import org.gradle.api.tasks.Copy
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.dokka)
}

kotlin {
    jvm()
    js(IR) {
        browser {
            commonWebpackConfig {
                sourceMaps = true
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
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
        }

        jsMain.dependencies {
        }

        wasmJsMain.dependencies { }

    }

    compilerOptions {
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
    }
}

// the package documents appear on the package pages of the API reference
dokka {
    dokkaSourceSets.configureEach {
        includes.from(fileTree(rootDir.resolve("docs/packages")) {
            include("kampfire.md", "kampfire.model.md")
        })
    }
}
