package streetlight.server.plugins

import io.ktor.server.application.Application
import io.ktor.server.http.content.resources
import io.ktor.server.http.content.static
import io.ktor.server.http.content.staticFiles
import io.ktor.server.http.content.staticResources
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kabinet.api.UserApi
import klutch.gemini.serveGemini
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
        serveSongPlays()
        servePages()
        serveRequests()
        serveGemini(Api.Gemini, app.gemini)
    }
}