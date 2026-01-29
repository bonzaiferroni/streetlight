import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
}

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                sourceMaps = true

                devServer?.static("/home/starfox/projects/streetlight", true)
                devServer?.proxy = mutableListOf(
                    KotlinWebpackConfig.DevServer.Proxy(
                        context = mutableListOf("/api"),
                        target = "http://localhost:8080",
                        changeOrigin = true,
                        secure = false
                    )
                )
            }
        }
        binaries.executable()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.html)
                implementation(kotlinWrappers.css)
                implementation(project(":koala"))
                implementation(project(":web"))
            }
        }

        val jsMain by getting {
            dependencies {
            }
        }
    }
}
