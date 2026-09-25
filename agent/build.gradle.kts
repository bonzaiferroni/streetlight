plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.serialization)
    application
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(project(":model"))
    implementation(project(":klutch"))
    implementation(project(":kabinet"))
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.apache5)
    implementation(libs.ktor.server.websockets)
    implementation(libs.exposed.core)

    implementation(libs.koog.agents)
    implementation(libs.koog.google.client)
    implementation(libs.fleeksoft.ksoup)
}

application {
    mainClass.set("MainKt")
}