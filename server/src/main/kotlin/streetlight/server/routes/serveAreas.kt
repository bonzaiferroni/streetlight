package streetlight.server.routes

import io.ktor.server.routing.Routing
import klutch.server.authenticateJwt
import klutch.server.get
import klutch.server.post
import klutch.utils.getUserId
import streetlight.model.Api
import streetlight.server.db.services.AreaDtoService
import streetlight.server.db.services.EventDtoService

fun Routing.serveAreas(
    service: AreaDtoService = AreaDtoService(),
) {
    get(Api.Areas) {
        service.readAreas()
    }

    authenticateJwt {
        post(Api.Areas.Create) { newArea, endpoint ->
            service.createArea(newArea)
        }
    }
}