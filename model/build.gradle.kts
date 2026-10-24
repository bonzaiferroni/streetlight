@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.dokka)
}

kotlin {
    js(IR).browser()
    jvm()
//    wasmJs {
//        browser()
//    }
    wasmJs {
        binaries.executable()
        browser()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                compileOnly(libs.compose.runtime)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)
                implementation(libs.ktor.http)
                implementation(libs.kotlinx.coroutines.core)
                api(project(":kampfire"))
                implementation(project(":kabinet"))
                api(project(":koala"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }

    compilerOptions {
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
    }
}

// the package documents appear on the package pages of the API reference
dokka {
    dokkaSourceSets.configureEach {
        includes.from(fileTree(rootDir.resolve("docs/packages")) {
            include("streetlight.model.md")
        })
    }
}
