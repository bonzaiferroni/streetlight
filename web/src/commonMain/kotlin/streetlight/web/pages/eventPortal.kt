package streetlight.web.pages

import koala.html.*
import kotlinx.html.*
import streetlight.model.data.*
import koala.css.*

fun HTML.eventPortal(event: Event, person: Person?, requestItems: List<RequestItem>) {
    head(event.title) {
        styles("eventPortal.css")
        scripts("eventportal.js")
    }
    body {
        column(Id("event-profile"), modify(AlignItemsCenter)) {
            a("/") {
                row {
                    heading4("Streetlight")
                    logo(1.5f)
                }
            }
            heading1(event.title)
            column(modify(Gap0)) {
                propertyValue("performer", "Luke Bollwerk")
                propertyValue("instagram") {
                    a("https://www.instagram.com/trespasserswilliam/") {
                        textBlock("trespasserswilliam")
                    }
                }
            }
            heading2("Send a request")
            column(Id("request-box"), modify(Width100)) {
                column(Id("request-songs")) {
                    requestItems.forEach { item ->
                        requestItem(item, event)
                        // button(song.title, invoke("startRequest", event.eventId.value, song.songId.value))
                    }
                }
                column(Id("request-details"), modify(DisplayNone)) {
                    textField(Id("name"), "Your name (optional)")
                    textField(Id("comment"), "Comment (optional)")
                    checkBox(Id("join"), "Would you like to sing with me?")
                    button("Send", invoke("sendRequest"))
                }
                column(Id("request-sent"), modify(DisplayNone)) {
                    textBlock("Request sent!")
                }
            }
        }
        column(Id("tips-box"), modify(AlignItemsCenter)) {
            heading2("Send a tip")
            row(modify(AlignItemsCenter)) {
                row {
                    heading3("Venmo:", modify(Opacity6))
                    a("https://venmo.com/colfaxband?txn=pay&note=street+music") {
                        heading3("@colfaxband", modify(Glow))
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
            column(modify(Flex1, Gap0)) {
                textBlock(song.title, modify(Bold))
                textBlock(song.artist)
            }
            column(modify(Gap0, AlignItemsCenter)) {
                textBlock("plays", modify(Opacity6))
                textBlock(plays.toString())
            }
        }
    }
}
