import org.gradle.api.tasks.Copy
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
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
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.coroutines.core)
                implementation(project(":model"))
                implementation(project(":koala"))
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(libs.kotlinx.html.js)
            }
        }
    }
}

// Where ye want the built JS to land
val webOutDir = layout.projectDirectory.dir("../server/www/core/js/compiled")

tasks.register<Copy>("copyBrowserJs") {
    // Grab webpack outputs (development + production if both exist)
    from(layout.buildDirectory.dir("kotlin-webpack/js/productionExecutable"))

    // Copy into yer chosen folder
    into(webOutDir)

    // Optional: only take what ye care about
    include("*.js", "*.js.map", "*.css", "*.css.map", "*.html")
    println("copying files")
}

tasks.named("jsBrowserProductionWebpack") {
    finalizedBy("copyBrowserJs")
}
