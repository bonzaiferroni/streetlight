package streetlight.server

import io.ktor.server.application.*
import klutch.environment.readEnvFromPath
import klutch.server.configureSecurity
import streetlight.server.plugins.*

//val host = "https://streetlight.ing"

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureCors()
    configureSerialization()
    configureDatabases()
    configureSecurity()
    configureApiRoutes()
    configureWebSockets()
    configureLogging()
}

object RuntimeProvider: ServerProvider {
    override val env = readEnvFromPath()
    override val dao = ServerDao()
    override val service = ServerService()
}