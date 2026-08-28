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

val standalones = file("standalones.txt").readText()
    .split(",", "\n")
    .map { it.trim() }
    .filter { it.isNotEmpty() }

fun String.capital() = replaceFirstChar { it.uppercase() }

kotlin {
    js {
        browser {
            commonWebpackConfig {
                sourceMaps = true
            }
        }
        binaries.executable()

        standalones.forEach { name ->
            val standalone = compilations.create(name) {
                associateWith(this@js.compilations.getByName("main"))
                defaultSourceSet {
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
        }

        wasmJsMain.dependencies {
        }
    }

    compilerOptions {
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
    }
}

tasks.withType<KotlinJsIrLink>().configureEach {
    val taskName = name
    if (standalones.any { taskName.startsWith("compile${it.capital()}") }) {
        compilerOptions.main.set(JsMainFunctionExecutionMode.CALL)
    }
}

listOf("Development", "Production").forEach { mode ->
    tasks.named("jsBrowser${mode}Webpack") {
        standalones.forEach { name ->
            val capital = name.capital()
            dependsOn("js$capital$capital${mode}ExecutableCompileSync")
            inputs.dir(layout.buildDirectory.dir("compileSync/js/$name/$name${mode}Executable/kotlin"))
                .withPropertyName("$name${mode}Bundle")
        }
    }
}