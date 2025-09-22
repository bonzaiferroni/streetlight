package streetlight.server.routes

import io.ktor.server.routing.Routing
import klutch.server.authenticateJwt
import klutch.server.get
import klutch.server.post
import streetlight.model.Api
import streetlight.server.RuntimeProvider
import streetlight.server.ServerProvider

fun Routing.serveAreas(app: ServerProvider = RuntimeProvider) {
    val dao = app.dao.area

    get(Api.Areas) {
        dao.readAreas()
    }

    authenticateJwt {
        post(Api.Areas.Create) { newArea, endpoint ->
            dao.createArea(newArea)
        }
    }
}