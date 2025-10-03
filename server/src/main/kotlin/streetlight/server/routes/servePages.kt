package streetlight.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.html.respondHtml
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import kotlinx.html.*
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
            head {
                title { +event.title }
                link { rel = "stylesheet"; href = "/static/styles.css" }
                script(src = "/static/eventportal.js") {}
            }
            body {
                h1 { +event.title }
                p { +"Performer: ${spark?.stageName}" }
                h2 { +"Requests" }
                div {
                    id = "request-box"
                    div {
                        id = "request-songs"
                        songs.forEach { song ->
                            button {
                                onClick = "startRequest('${event.eventId.value}', '${song.songId.value}')"
                                +song.title
                            }
                        }
                    }
                    div {
                        id = "request-details"
                        hidden = true

                        label {
                            checkBoxInput { id = "join"; name = "join" }
                            +" Join me?"
                        }

                        br {}

                        textInput { id = "name"; name = "name"; placeholder = "Your name (optional)" }

                        br {}

                        textInput { id = "comment"; name = "comment"; placeholder = "Comment (optional)" }

                        br {}

                        button { onClick = "sendRequest()"; +"Send" }
                    }
                }
            }
        }
    }
}