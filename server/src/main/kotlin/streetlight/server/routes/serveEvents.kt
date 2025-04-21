package streetlight.server.routes

import io.ktor.server.routing.Routing
import klutch.server.*
import klutch.utils.getUserId
import streetlight.model.Api
import streetlight.server.db.services.EventDtoService

fun Routing.serveEvents(
    service: EventDtoService = EventDtoService(),
) {
    get(Api.Events) {
        service.readActiveEvents()
    }

    authenticateJwt {
        post(Api.Events.Create) { newEvent, endpoint ->
            val userId = call.getUserId()
            service.createEvent(userId, newEvent)
        }
    }
}