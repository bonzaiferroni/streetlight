package streetlight.server.pages

import klutch.html.*
import kotlinx.html.*
import streetlight.model.data.*

fun HTML.eventPortal(event: Event, spark: Spark?, requestItems: List<RequestItem>) {
    head(event.title) {
        coreStyles()
        coreScripts()
        styles("eventPortal.css")
        scripts("eventportal.js")
    }
    body {
        column(modify(AlignItemsCenter)) {
            heading1(event.title)
            column(modify(Gap0)) {
                row {
                    paragraph("performer:", modify(DimText))
                    paragraph("Luke Bollwerk")
                }
                row {
                    paragraph("instagram:", modify(DimText))
                    a("https://www.instagram.com/trespasserswilliam/") {
                        paragraph("trespasserswilliam")
                    }
                }
            }
            heading2("Send a request")
            column(modify(FillWidth)) {
                set(Id("request-box"))
                column {
                    set(Id("request-songs"))
                    requestItems.forEach { item ->
                        requestItem(item, event)
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
    item: RequestItem,
    event: Event,
) {
    val (song, plays) = item
    card() {
        onClick = invoke("startRequest", event.eventId.value, song.songId.value)
        row() {
            column(modify(Flex1, Gap0)) {
                paragraph(song.title, modify(Bold))
                paragraph(song.artist)
            }
            column(modify(Gap0, AlignItemsCenter)) {
                paragraph("plays", modify(DimText))
                paragraph(plays.toString())
            }
        }
    }
}

object TipsBox: CssClass { override val value = "tips-box" }