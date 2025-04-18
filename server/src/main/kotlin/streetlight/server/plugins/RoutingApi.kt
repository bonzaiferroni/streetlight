package streetlight.server.plugins

import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kabinet.api.UserApi
import klutch.db.services.UserDtoService
import klutch.server.*
import streetlight.model.Api
import streetlight.model.apiPrefix
import streetlight.server.routes.serveAreas
import streetlight.server.routes.serveEvents
import streetlight.server.routes.serveLocations

fun Application.configureApiRoutes() {
    routing {
        get(apiPrefix) {
            call.respondText("Hello World!")
        }

        post(UserApi.Login.path) {
            call.authorize()
        }

        serveUsers()
        serveEvents()
        serveAreas()
        serveLocations()
    }
}