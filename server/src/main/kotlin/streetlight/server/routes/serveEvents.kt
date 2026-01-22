package streetlight.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.html.respondHtml
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import kabinet.model.GeoPoint
import klutch.server.*
import klutch.utils.getUserId
import kotlinx.datetime.Clock
import kotlinx.html.body
import kotlinx.html.p
import streetlight.model.APP_API_URL
import streetlight.model.Api
import streetlight.model.MockDb
import streetlight.model.data.Performer
import streetlight.model.data.PerformerId
import streetlight.model.data.toProjectId
import streetlight.model.mockDb
import streetlight.server.RuntimeProvider
import streetlight.server.ServerProvider

fun Routing.serveEvents(app: ServerProvider = RuntimeProvider) {
    val dao = app.dao.event

    getEndpoint(Api.EventFeed) {
        dao.readActiveEvents()
    }

    getEndpoint(Api.EventProfile, { it.toProjectId() }) { id, _ ->
        dao.readEvent(id)
    }

    queryEndpoint(Api.EventFeed.LocationEvents, GeoPoint::fromQuery) { sent, endpoint ->
        val events = mockDb.events
        events.take((1 until events.size).random())
    }

    get("/qr") {
        val event = dao.readActiveEvents().firstOrNull()
        if (event == null) {
            call.respondHtml(HttpStatusCode.NotFound) { body { p { +"Arr, no active events!" } } }
        } else {
            call.respondRedirect("/eventportal/${event.eventId.value}")
        }
    }

    authenticateJwt {
        postEndpoint(Api.EventFeed.Create) { newEvent, _ ->
            val userId = getUserId()
            dao.createEvent(userId, newEvent)
        }

        updateEndpoint(Api.EventProfile.Update) { update, _ ->
            val userId = getUserId()
            dao.updateEvent(userId, update)
        }

        deleteEndpoint(Api.EventFeed.Delete) { eventId, _ ->
            val userId = getUserId()
            dao.deleteEvent(userId, eventId)
        }

//        webSocket(Api.Events.UserEvents.path) {
//            val userId = call.getUserId()
//
//        }
    }
}