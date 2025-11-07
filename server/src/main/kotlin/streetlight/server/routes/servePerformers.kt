package streetlight.server.routes

import io.ktor.server.routing.Routing
import streetlight.server.RuntimeProvider
import streetlight.server.ServerProvider

fun Routing.servePerformers(app: ServerProvider = RuntimeProvider) {
    // Hook up performer endpoints here when Api.Performer is defined
    // Example pattern:
    // val dao = app.dao.performer
    // getEndpoint(Api.PerformerFeed, { it.toProjectId() }) { id, _ ->
    //     dao.readById(id)
    // }
}
