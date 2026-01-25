plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
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
                implementation(libs.kotlinx.html)
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(libs.kotlinx.html.js)
            }
        }
    }
}
