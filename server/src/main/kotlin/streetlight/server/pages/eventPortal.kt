package streetlight.server.pages

import klutch.html.*
import kotlinx.html.*
import streetlight.model.data.*

fun HTML.eventPortal(event: Event, spark: Spark?, requestItems: List<RequestItem>) {
    head(event.title) {
        styles("eventPortal.css")
        scripts("eventportal.js")
    }
    body {
        column(Id("event-profile"), AlignItemsCenter) {
            a("/") {
                row {
                    heading4("Streetlight")
                    logo(1.5f)
                }
            }
            heading1(event.title)
            column(NoGap) {
                propertyValue("performer", "Luke Bollwerk")
                propertyValue("instagram") {
                    a("https://www.instagram.com/trespasserswilliam/") {
                        paragraph("trespasserswilliam")
                    }
                }
            }
            heading2("Send a request")
            column(Id("request-box"), FillWidth) {
                column(Id("request-songs")) {
                    requestItems.forEach { item ->
                        requestItem(item, event)
                        // button(song.title, invoke("startRequest", event.eventId.value, song.songId.value))
                    }
                }
                column(Id("request-details"), DisplayNone) {
                    textField(Id("name"), "Your name (optional)")
                    textField(Id("comment"), "Comment (optional)")
                    checkBox(Id("join"), "Would you like to sing with me?")
                    button("Send", invoke("sendRequest"))
                }
                column(Id("request-sent"), DisplayNone) {
                    paragraph("Request sent!")
                }
            }
        }
        column(Id("tips-box"), AlignItemsCenter) {
            heading2("Send a tip")
            row(AlignItemsCenter) {
                row {
                    heading3("Venmo:", Dim)
                    a("https://venmo.com/colfaxband?txn=pay&note=street+music") {
                        heading3("@colfaxband", Glow)
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
    card {
        onClick = invoke("startRequest", event.eventId.value, song.songId.value)
        row {
            column(Flex1, NoGap) {
                paragraph(song.title, Bold)
                paragraph(song.artist)
            }
            column(NoGap, AlignItemsCenter) {
                paragraph("plays", Dim)
                paragraph(plays.toString())
            }
        }
    }
}
