import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.serialization)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.navigation3.ui)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel.navigation3)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(project(":model"))
            implementation(project(":kabinet"))
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.cio)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.serialization.cbor)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            implementation(libs.maplibre.compose)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs) {
                exclude(group = "org.jetbrains.compose.material")
            }
            implementation(libs.kotlinx.coroutines.swing)
            runtimeOnly(libs.maplibre.compose.runtime.vulkan.linux.x64)
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

// the app's resources, with the site's icons from www/icon as files/icon and its drawings from www/svg as files/svg
val syncComposeResources by tasks.registering(Sync::class) {
    from("src/commonMain/composeResources")
    from(rootProject.layout.projectDirectory.dir("www/icon")) {
        into("files/icon")
    }
    from(rootProject.layout.projectDirectory.dir("www/svg")) {
        into("files/svg")
    }
    into(layout.buildDirectory.dir("generated/appComposeResources"))
}

compose.resources {
    packageOfResClass = "streetlight.compose.resources"
    customDirectory("commonMain", layout.dir(syncComposeResources.map { it.destinationDir }))
}

compose.desktop {
    application {
        mainClass = "streetlight.compose.MainKt"
        // MapLibre's native renderer
        jvmArgs += "--enable-native-access=ALL-UNNAMED"

        nativeDistributions {
            targetFormats(TargetFormat.Deb, TargetFormat.Dmg, TargetFormat.Msi)
            packageName = "Streetlight"
            packageVersion = "1.0.0"
        }
    }
}
