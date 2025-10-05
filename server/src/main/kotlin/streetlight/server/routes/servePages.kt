package streetlight.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.html.respondHtml
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import kotlinx.html.*
import klutch.html.*
import streetlight.model.data.EventId
import streetlight.server.RuntimeProvider
import streetlight.server.ServerProvider

fun Routing.servePages(app: ServerProvider = RuntimeProvider) {
    get("/eventportal/{id}") {
        val eventId = call.parameters["id"]?.let { EventId(it) } ?: return@get
        val event = app.dao.event.readEvent(eventId) ?: return@get
        val spark = app.dao.spark.readByUserId(event.userId)
        val songs = app.dao.song.readAllByUserId(event.userId)
        call.respondHtml(HttpStatusCode.OK) {
            head(event.title) {
                styles("styles.css")
                scripts("eventportal.js", "helpers.js")
            }
            body {
                h1(event.title)
                p("Performer: ${spark?.stageName}")
                h2("Requests")
                div(Id("request-box")) {
                    div(Id("request-songs"), classes(Column)) {
                        songs.forEach { song ->
                            button(song.title, invoke("startRequest", event.eventId.value, song.songId.value))
                        }
                    }
                    div(Id("request-details"), classes(Column, Hidden)) {
                        checkBox(Id("join"), "Join me?")
                        textField(Id("name"), "Your name (optional)")
                        textField(Id("comment"), "Comment (optional)")
                        button("Send", invoke("sendRequest"))
                    }
                    div(Id("request-sent"), classes(Column, Hidden)) {
                        p("Request sent!")
                    }
                }
            }
        }
    }
}