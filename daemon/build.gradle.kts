plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.serialization)
    application
}

group = "streetlight.server.daemon"
version = "0.0.1"

application {
    mainClass.set("streetlight.server.daemon.MainKt")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(project(":server"))
    implementation(project(":model"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}