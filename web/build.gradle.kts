@file:OptIn(ExperimentalDistributionDsl::class, ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JsMainFunctionExecutionMode
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalDistributionDsl
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrLink

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    kotlin("plugin.js-plain-objects") version "2.3.10"
}

kotlin {
    js {
        browser {
            commonWebpackConfig {
                sourceMaps = true
            }
            // distribution {
            //     outputDirectory.set(projectDir.resolve("../www/js/streetlight"))
            // }
        }
        binaries.executable()

        listOf("passwordReset").forEach { name ->
            val standalone = compilations.create(name) {
                associateWith(this@js.compilations.getByName("main"))
                defaultSourceSet {
                    println("building $name")
                    kotlin.setSrcDirs(listOf("src/jsStandalone/$name/kotlin"))
                }
            }
            binaries.executable(standalone)
        }
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
            implementation(kotlinWrappers.browser)
            // implementation(npm("@js-joda/timezone", "2.23.0"))
        }

        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
    }

    compilerOptions {
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
    }
}

tasks.named("jsBrowserDevelopmentWebpack") {
    dependsOn("jsPasswordResetPasswordResetDevelopmentExecutableCompileSync")
}

tasks.named("jsBrowserProductionWebpack") {
    dependsOn("jsPasswordResetPasswordResetProductionExecutableCompileSync")
}

tasks.withType<KotlinJsIrLink>().configureEach {
    if (name.contains("PasswordReset")) {
        compilerOptions.main.set(JsMainFunctionExecutionMode.CALL)
    }
}

tasks.named("jsBrowserDevelopmentWebpack") {
    inputs.dir(
        layout.buildDirectory.dir(
            "compileSync/js/passwordReset/passwordResetDevelopmentExecutable/kotlin"
        )
    ).withPropertyName("passwordResetBundleInput")
}

tasks.named("jsBrowserProductionWebpack") {
    inputs.dir(
        layout.buildDirectory.dir(
            "compileSync/js/passwordReset/passwordResetProductionExecutable/kotlin"
        )
    ).withPropertyName("passwordResetBundleInput")
}