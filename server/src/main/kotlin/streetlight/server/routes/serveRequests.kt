package streetlight.server.routes

import io.ktor.server.routing.Routing
import klutch.server.postEndpoint
import streetlight.model.Api
import streetlight.server.RuntimeProvider
import streetlight.server.ServerProvider

fun Routing.serveRequests(app: ServerProvider = RuntimeProvider) {

    postEndpoint(Api.RequestBox) { newRequest, _ ->
        app.dao.request.createRequest(newRequest)?.requestId
    }
}