rootProject.name = "streetlight"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    versionCatalogs {
        create("kotlinWrappers") {
            val wrappersVersion = "2026.1.11"
            from("org.jetbrains.kotlin-wrappers:kotlin-wrappers-catalog:$wrappersVersion")
        }
    }
}

include(":pondui")
project(":pondui").projectDir = file("pondui/library")
include(":kabinet")
project(":kabinet").projectDir = file("kabinet/library")
include(":klutch")
project(":klutch").projectDir = file("klutch/library")

include(":app")
include(":model")
include(":server")
include(":kampfire")

include(":konch")
include(":koala")
include(":web")
//include(":webdev")
include(":agent")
include(":webscripts")