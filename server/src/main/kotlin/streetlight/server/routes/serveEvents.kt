package streetlight.server.routes

import io.ktor.server.routing.Routing
import klutch.server.*
import klutch.utils.getUserId
import streetlight.model.Api
import streetlight.model.data.toProjectId
import streetlight.server.RuntimeProvider
import streetlight.server.ServerProvider

fun Routing.serveEvents(app: ServerProvider = RuntimeProvider) {
    val dao = app.dao.event

    get(Api.EventFeed) {
        dao.readActiveEvents()
    }

    get(Api.EventProfile, { it.toProjectId() }) { id, _ ->
        dao.readEvent(id)
    }

    authenticateJwt {
        post(Api.EventFeed.Create) { newEvent, _ ->
            val userId = getUserId()
            dao.createEvent(userId, newEvent)
        }

        update(Api.EventProfile.Update) { update, _ ->
            val userId = getUserId()
            dao.updateEvent(userId, update)
        }

        delete(Api.EventFeed.Delete) { eventId, _ ->
            val userId = getUserId()
            dao.deleteEvent(userId, eventId)
        }

//        webSocket(Api.Events.UserEvents.path) {
//            val userId = call.getUserId()
//
//        }
    }
}