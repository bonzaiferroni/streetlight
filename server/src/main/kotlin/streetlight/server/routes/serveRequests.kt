package streetlight.server.routes

import io.ktor.server.routing.Routing
import klutch.server.post
import streetlight.model.Api
import streetlight.server.RuntimeProvider
import streetlight.server.ServerProvider

fun Routing.serveRequests(app: ServerProvider = RuntimeProvider) {

    post(Api.RequestBox) { newRequest, _ ->
        app.dao.request.createRequest(newRequest)?.requestId
    }
}