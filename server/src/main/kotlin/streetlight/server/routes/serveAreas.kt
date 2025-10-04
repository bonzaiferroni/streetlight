package streetlight.server.routes

import io.ktor.server.routing.Routing
import klutch.server.authenticateJwt
import klutch.server.getEndpoint
import klutch.server.postEndpoint
import streetlight.model.Api
import streetlight.server.RuntimeProvider
import streetlight.server.ServerProvider

fun Routing.serveAreas(app: ServerProvider = RuntimeProvider) {
    val dao = app.dao.area

    getEndpoint(Api.AreaFeed) {
        dao.readAreas()
    }

    authenticateJwt {
        postEndpoint(Api.AreaFeed.Create) { newArea, endpoint ->
            dao.createArea(newArea)
        }
    }
}