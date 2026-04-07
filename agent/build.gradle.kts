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
    implementation(libs.ktor.server.websockets)
    implementation(libs.exposed.core)

    implementation("ai.koog:koog-agents:0.7.3")
    implementation("com.fleeksoft.ksoup:ksoup:0.2.5")
}

application {
    mainClass.set("MainKt")
}