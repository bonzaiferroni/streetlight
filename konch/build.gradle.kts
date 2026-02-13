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
        binaries.executable()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(libs.kotlinx.html.js)
            }
        }
    }
}
