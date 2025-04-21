package streetlight.server.plugins

import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kabinet.api.UserApi
import klutch.server.*
import streetlight.model.apiPrefix
import streetlight.server.routes.*

fun Application.configureApiRoutes() {
    routing {
        get(apiPrefix) {
            call.respondText("Hello World!")
        }

        serveUsers()
        serveEvents()
        serveAreas()
        serveLocations()
        serveSongs()
    }
}