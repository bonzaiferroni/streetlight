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
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.html)
                implementation(kotlinWrappers.css)
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(libs.kotlinx.html.js)
            }
        }
    }
}
