package streetlight.server.pages

import klutch.html.*
import kotlinx.html.*
import streetlight.model.data.*

fun HTML.eventPortal(event: Event, spark: Spark?, songs: List<Song>) {
    head(event.title) {
        coreStyles()
        coreScripts()
        styles("eventPortal.css")
        scripts("eventportal.js")
    }
    body {
        column(modify(AlignItemsCenter)) {
            heading1(event.title)
            paragraph("Performer: ${spark?.stageName}")
            heading2("Send a request")
            column(modify(FillWidth)) {
                set(Id("request-box"))
                column {
                    set(Id("request-songs"))
                    songs.forEach { song ->
                        requestItem(song, event)
                        // button(song.title, invoke("startRequest", event.eventId.value, song.songId.value))
                    }
                }
                column(modify(DisplayNone)) {
                    set(Id("request-details"))
                    checkBox(Id("join"), "Join me?")
                    textField(Id("name"), "Your name (optional)")
                    textField(Id("comment"), "Comment (optional)")
                    button("Send", invoke("sendRequest"))
                }
                column(modify(DisplayNone)) {
                    set(Id("request-sent"))
                    paragraph("Request sent!")
                }
            }
        }
        column(modify(TipsBox, AlignItemsCenter)) {
            heading2("Send a tip")
            row(modify(AlignItemsCenter)) {
                row {
                    heading3("Venmo:", modify(DimText))
                    a("https://venmo.com/colfaxband?txn=pay&note=street+music") {
                        heading3("@colfaxband")
                    }
                }
                a("https://venmo.com/colfaxband?txn=pay&amount=1&note=street+music") {
                    button("$1")
                }
                a("https://venmo.com/colfaxband?txn=pay&amount=1&note=street+music") {
                    button("$5")
                }
                a("https://venmo.com/colfaxband?txn=pay&note=street+music") {
                    button("Other")
                }
            }
            heading4("Thank you for stopping by!")
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
            column(modify(Flex1, Gap0)) {
                paragraph(song.title, modify(Bold))
                paragraph(song.artist)
            }
        }
    }
}

object TipsBox: CssClass { override val value = "tips-box" }