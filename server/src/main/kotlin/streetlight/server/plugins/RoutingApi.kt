package streetlight.server.plugins

import io.ktor.server.application.Application
import io.ktor.server.http.content.staticFiles
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import klutch.gemini.serveSpeech
import klutch.server.*
import streetlight.model.Api
import streetlight.server.RuntimeProvider
import streetlight.server.ServerProvider
import streetlight.server.routes.*
import java.io.File

fun Application.configureApiRoutes(app: ServerProvider = RuntimeProvider) {
    routing {
        get(Api.path) {
            call.respondText("Hello Colfax!")
        }

        staticFiles("/static", File("www"))

        serveUsers()
        serveEvents()
        serveAreas()
        serveLocations()
        serveSongs()
        serveRenditions()
        servePages()
        serveRequests()
        servePerformers()
        // serveGemini(Api.Gemini, app.gemini)
        serveSpeech(Api.Speech, app.speech)
    }
}