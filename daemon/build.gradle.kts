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
    implementation(libs.kotlinx.datetime)

    testImplementation(kotlin("test"))

    implementation(project(":server"))
    implementation(project(":model"))
    implementation(project(":kabinet"))
    implementation(project(":klutch"))
    implementation(project(":kampfire"))
    implementation(project(":koala"))
    implementation(project(":agent"))

    implementation(libs.fleeksoft.ksoup)
    implementation("com.vladsch.flexmark:flexmark-html2md-converter:0.64.8")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}