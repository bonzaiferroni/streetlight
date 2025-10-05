package streetlight.server.pages

import klutch.html.*
import kotlinx.html.*
import streetlight.model.data.*

fun HTML.eventPortal(event: Event, spark: Spark?, songs: List<Song>) {
    head(event.title) {
        coreStyles()
        coreScripts()
        scripts("eventportal.js")
    }
    body {
        column(modifiers = modify(AlignItemsCenter)) {
            heading1(event.title)
            paragraph("Performer: ${spark?.stageName}")
            heading2("Send a request")
            column(Id("request-box"), modify(FillWidth)) {
                column(Id("request-songs")) {
                    songs.forEach { song ->
                        requestItem(song, event)
                    }
                }
                column(Id("request-details"), modify(DisplayNone)) {
                    checkBox(Id("join"), "Join me?")
                    textField(Id("name"), "Your name (optional)")
                    textField(Id("comment"), "Comment (optional)")
                    button("Send", invoke("sendRequest"))
                }
                column(Id("request-sent"), modify(DisplayNone)) {
                    paragraph("Request sent!")
                }
            }
        }
    }
}

fun FlowContent.requestItem(
    song: Song,
    event: Event,
) {
    card() {
        onClick = invoke("startRequest", event.eventId.value, song.songId.value)
        row() {
            column(modifiers = modify(Flex1, Gap0)) {
                paragraph(song.title, modify(Bold))
                paragraph(song.artist)
            }
        }
    }
}