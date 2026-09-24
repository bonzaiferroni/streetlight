package streetlight.web.pages

import koala.FileSet
import koala.Asset
import koala.PageResource
import koala.html.*
import kotlinx.html.*
import streetlight.model.data.*
import koala.modifier.*
import kotlinx.css.LinearDimension
import kotlinx.css.rem

/** A standalone page for requesting songs at [event]. */
fun HTML.eventPortal(event: Event, performer: Performer?, requestItems: List<RequestItem>, resource: PageResource) {
    appHead(event.title, resource) {
        applyFiles(EventPortalFiles)
//        applyScripts("event-portal/webscripts.js")
    }
    body {
        column(Id("event-profile"), modify(AlignItemsCenter, Padding(1), PaddingBottom(8.rem))) {
            setAttribute(EventPortalSelector.eventIdAttribute, event.eventId)
            a("/") {
                row() {
                    textLogo()
                    heading4("Streetlight")
                }
            }
            spacer()
            heading1(event.title)
            column(modify(Gap0, Width(LinearDimension.auto))) {
                textProperty("performer", "Luke Bollwerk")
                textProperty("instagram") {
                    a("https://www.instagram.com/trespasserswilliam/") {
                        textBlock("trespasserswilliam")
                    }
                }
            }
            tabs(Id("event-portal-tabs"), Width(64)) {
                tab("bio") {
                    textBlock("yer bio")
                }
                tab("requests") {
                    column(Id("request-box"), AlignItemsCenter) {
                        column(Id("request-songs")) {
                            requestItems.forEach { item ->
                                requestItem(item)
                                // button(song.title, invoke("startRequest", event.eventId.value, song.songId.value))
                            }
                        }
                        column(Id("request-details"), DisplayNone) {
                            textField(id = Id("name"), placeholder = "Your name (optional)")
                            textField(id = Id("comment"), placeholder = "Comment (optional)")
                            checkBox(Id("join"), "Would you like to sing with me?")
                            btn("Send", invoke("sendRequest"))
                        }
                        column(Id("request-sent"), DisplayNone) {
                            textBlock("Request sent!")
                        }
                    }
                }
                tab("shows") {
                    textBlock("yer shows")
                }
            }

        }
        column(Id("tips-box"), AlignItemsCenter) {
            row(modify(AlignItemsCenter, Width(LinearDimension.auto))) {
                row(Width(LinearDimension.auto)) {
                    heading3("Venmo:", OpacityHigh)
                    a("https://venmo.com/colfaxband?txn=pay&note=street+music") {
                        heading3("@colfaxband", GlowShadow)
                    }
                }
//                a("https://venmo.com/colfaxband?txn=pay&amount=1&note=street+music") {
//                    button("$1", mod = modify(Accent))
//                }
//                a("https://venmo.com/colfaxband?txn=pay&amount=1&note=street+music") {
//                    button("$5", mod = modify(Accent))
//                }
                btn(
                    text = "Send a tip",
                    href = "https://venmo.com/colfaxband?txn=pay&note=street+music",
                    mod = Accent,
                    addFlair = false,
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
        onClick = invoke("startRequest", song.songId.value.toString())

        row(Flex1) {
            column(modify(Flex1, Gap0, Width(LinearDimension.auto))) {
                textBlock(song.title, Bold)
                textBlock(song.artist)
            }
            column(modify(Gap0, AlignItemsCenter, Width(LinearDimension.auto))) {
                textBlock("plays", OpacityHigh)
                textBlock(plays.toString())
            }
        }
    }
}

object EventPortalSelector {
    val requestItem = Class("request-item")
    val eventIdAttribute = idAttributeOf("event-id") { EventId(it) }
    val songIdAttribute = idAttributeOf("song-id") { SongId(it) }
    val sendRequestButtonId = Id("send-request-button")
}

object EventPortalFiles: FileSet<Asset>() {
    val css = add("event-portal.css")
    val js = add("event-portal.js")
}