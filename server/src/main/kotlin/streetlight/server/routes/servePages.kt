package streetlight.server.routes

import io.ktor.http.ContentType
import io.ktor.http.defaultForFilePath
import io.ktor.server.html.respondHtml
import io.ktor.server.http.content.staticFiles
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import kabinet.console.globalConsole
import streetlight.model.data.EventId
import streetlight.server.RuntimeProvider
import streetlight.server.ServerProvider
import streetlight.web.pages.eventPage
import streetlight.web.pages.eventPortal
import streetlight.web.pages.eventSignUp
import streetlight.web.pages.homePage
import java.io.File

private val console = globalConsole.getHandle(Routing::servePages.name)

fun Routing.servePages(app: ServerProvider = RuntimeProvider) {
    uploadFolder.mkdirs()
    wwwFolder.mkdirs()
    staticFiles("/upload", uploadFolder)
    staticFiles("/www", wwwFolder) {
        contentType { file ->
            if (file.extension == "map") ContentType.Application.Json
            else ContentType.defaultForFilePath(file.path)
        }
//        cacheControl {
//            listOf(CacheControl.MaxAge(maxAgeSeconds = 600))
//        }
    }

    get("/") {
        val events = app.dao.event.readActiveEvents()
        call.respondHtml {
            homePage(events)
        }
    }

    get("/event-portal/{id}") {
        val eventId = call.parameters["id"]?.let { EventId(it) } ?: return@get
        val event = app.dao.event.readEvent(eventId) ?: return@get
        val spark = app.dao.spark.readByUserId(event.userId)
        val requestItems = app.dao.song.readRequestItems(event.userId)
        call.respondHtml {
            console.log("responding")
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

    get("/event/{id}") {
        val eventId = call.parameters["id"]?.let { EventId(it) } ?: return@get
        val event = app.dao.event.readEvent(eventId) ?: return@get

        call.respondHtml {
            eventPage(event)
        }
    }
}

val uploadFolder = File("../upload")
val wwwFolder = File("../www")