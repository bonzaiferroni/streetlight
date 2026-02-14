package streetlight.server.routes

import io.ktor.server.routing.Routing
import kabinet.console.globalConsole
import klutch.server.getEndpoint
import klutch.server.readParam
import kotlinx.datetime.Instant
import streetlight.model.Api
import streetlight.server.RuntimeProvider
import streetlight.server.ServerProvider

private val console = globalConsole.getHandle(Routing::serveStories.name)

fun Routing.serveStories(app: ServerProvider = RuntimeProvider) {
    getEndpoint(Api.Stories.ReadUrl) { endpoint ->
        readParam(endpoint.url)
    }
}