package streetlight.server.routes

import io.ktor.server.routing.Routing
import io.ktor.server.websocket.webSocket
import klutch.server.*
import klutch.utils.getUserId
import streetlight.model.Api
import streetlight.server.RuntimeProvider
import streetlight.server.ServerProvider

fun Routing.serveEvents(app: ServerProvider = RuntimeProvider) {
    val dao = app.dao.event

    get(Api.Events) {
        dao.readActiveEvents()
    }

    authenticateJwt {
        post(Api.Events.Create) { newEvent, _ ->
            val userId = call.getUserId()
            dao.createEvent(userId, newEvent)
        }

        update(Api.Events.Update) { update, _ ->
            val userId = call.getUserId()
            dao.updateEvent(userId, update)
        }

        delete(Api.Events.Delete) { eventId, _ ->
            val userId = call.getUserId()
            dao.deleteEvent(userId, eventId)
        }

//        webSocket(Api.Events.UserEvents.path) {
//            val userId = call.getUserId()
//
//        }
    }
}