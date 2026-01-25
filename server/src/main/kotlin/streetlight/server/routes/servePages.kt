package streetlight.server.routes

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readBytes
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.server.html.respondHtml
import io.ktor.server.http.content.staticFiles
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import streetlight.model.data.EventId
import streetlight.server.RuntimeProvider
import streetlight.server.ServerProvider
import streetlight.server.pages.eventPortal
import streetlight.server.pages.eventSignUp
import streetlight.server.pages.homePage
import java.io.File

fun Routing.servePages(app: ServerProvider = RuntimeProvider) {
    staticFiles("/static", File("www"))

    get("/") {
        call.respondHtml {
            homePage()
        }
    }

    get("/event-portal/{id}") {
        val eventId = call.parameters["id"]?.let { EventId(it) } ?: return@get
        val event = app.dao.event.readEvent(eventId) ?: return@get
        val spark = app.dao.spark.readByUserId(event.userId)
        val requestItems = app.dao.song.readRequestItems(event.userId)
        call.respondHtml {
            eventPortal(event, spark, requestItems)
        }
    }

    get("/event-signup/{id}") {
        val eventId = call.parameters["id"]?.let { EventId(it) } ?: return@get
        val event = app.dao.event.readEvent(eventId) ?: return@get
        call.respondHtml {
            eventSignUp(event)
        }
    }
}

