package streetlight.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.html.respondHtml
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import streetlight.model.data.MapQuery
import klutch.server.*
import klutch.utils.getUserId
import kotlinx.html.body
import kotlinx.html.p
import streetlight.model.Api
import streetlight.model.data.EventInfo
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

    queryEndpoint(Api.EventFeed.QueryMap, MapQuery::fromQuery) { sent, endpoint ->
        if (sent == null) return@queryEndpoint emptyList()
        val locations = mockDb.locations.filter { sent.bounds.contains(it.geoPoint) }
        val locationIds = locations.map { it.locationId }.toSet()
        val events = mockDb.events.filter { locationIds.contains(it.locationId) }
        events.map { event -> EventInfo.from(event, locations.first { it.locationId == event.locationId }) }
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