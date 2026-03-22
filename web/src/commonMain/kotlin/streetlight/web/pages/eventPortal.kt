package streetlight.web.pages

import koala.html.*
import kotlinx.html.*
import streetlight.model.data.*
import koala.css.*

fun HTML.eventPortal(event: Event, performer: Performer?, requestItems: List<RequestItem>) {
    head(event.title) {
        applyStyles("event-portal.css")
        applyScripts("event-portal.js")
//        applyScripts("event-portal/webscripts.js")
    }
    body {
        column(Id("event-profile"), modify(AlignItemsCenter, Padding1)) {
            attributes[EventPortalSelector.eventIdAttribute] = event.eventId
            a("/") {
                row() {
                    logo()
                    heading4("Streetlight")
                }
            }
            spacer()
            heading1(event.title)
            column(modify(Gap0, WidthAuto)) {
                propertyValue("performer", "Luke Bollwerk")
                propertyValue("instagram") {
                    a("https://www.instagram.com/trespasserswilliam/") {
                        textBlock("trespasserswilliam")
                    }
                }
            }
            tabs(Id("event-portal-tabs"), modify(Width64)) {
                tab("bio") {
                    textBlock("yer bio")
                }
                tab("requests", true) {
                    column(Id("request-box"), modify(AlignItemsCenter)) {
                        column(Id("request-songs")) {
                            requestItems.forEach { item ->
                                requestItem(item)
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
                tab("shows") {
                    textBlock("yer shows")
                }
            }

        }
        column(Id("tips-box"), modify(AlignItemsCenter)) {
            row(modify(AlignItemsCenter, WidthAuto)) {
                row(modify(WidthAuto)) {
                    heading3("Venmo:", modify(Opacity6))
                    a("https://venmo.com/colfaxband?txn=pay&note=street+music") {
                        heading3("@colfaxband", modify(GlowShadow))
                    }
                }
//                a("https://venmo.com/colfaxband?txn=pay&amount=1&note=street+music") {
//                    button("$1", modifiers = modify(Accent))
//                }
//                a("https://venmo.com/colfaxband?txn=pay&amount=1&note=street+music") {
//                    button("$5", modifiers = modify(Accent))
//                }
                button(
                    text = "Send a tip",
                    href = "https://venmo.com/colfaxband?txn=pay&note=street+music",
                    modifiers = modify(Accent),
                    addExternalIndicator = false,
                )
            }
            heading4("Thank you for stopping by!")
        }
    }
}

fun FlowContent.requestItem(
    item: RequestItem,
) {
    val (song, plays) = item
    card() {
//        attributes[EventPortalSelector.songIdAttribute] = song.songId
        onClick = invoke("startRequest", song.songId.value)

        row(modify(Flex1)) {
            column(modify(Flex1, Gap0, WidthAuto)) {
                textBlock(song.title, modify(Bold))
                textBlock(song.artist)
            }
            column(modify(Gap0, AlignItemsCenter, WidthAuto)) {
                textBlock("plays", modify(Opacity6))
                textBlock(plays.toString())
            }
        }
    }
}

object EventPortalSelector {
    val requestItem = Css("request-item")
    val eventIdAttribute = Attribute("event-id")
    val songIdAttribute = Attribute("song-id")
    val sendRequestButtonId = Id("send-request-button")
}
