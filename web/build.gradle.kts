import org.gradle.api.tasks.Copy
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
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
                implementation(libs.kotlinx.coroutines.core)
                kotlin("js")
            }
        }
    }
}

// Where ye want the built JS to land
val webOutDir = layout.projectDirectory.dir("../server/www/core/js")

tasks.register<Copy>("copyBrowserJs") {
    dependsOn("jsBrowserProductionWebpack") // <-- build first, then copy

    // Grab webpack outputs (development + production if both exist)
    from(layout.buildDirectory.dir("kotlin-webpack/js/productionExecutable"))

    // Copy into yer chosen folder
    into(webOutDir)

    // Optional: only take what ye care about
    include("*.js", "*.js.map", "*.css", "*.css.map", "*.html")
}
