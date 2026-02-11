package streetlight.web.pages

import koala.html.*
import kotlinx.html.*
import koala.css.*
import streetlight.web.AccountRoute
import streetlight.web.FullMapRoute
import streetlight.web.HomeRoute

fun HTML.singlePage() {
    head("Streetlight | Home") {
        styles("homePage.css")
        link(href = "https://cdn.jsdelivr.net/npm/maplibre-gl@5.12.0/dist/maplibre-gl.css", "stylesheet")
        koalaStyles()
    }
    body {
        box(SinglePageId.viewportBox) {
            column(SinglePageId.app) {
                column(modify(Width100, AlignItemsCenter)) {
                    row(modify(Width100, SpaceBetween)) {
                        action(FullMapRoute, modify(Height6, Opacity2)) {
                            icon("chevron-down", modify(Height100))
                        }
                        action(HomeRoute()) {
                            row {
                                logo()
                                heading1("Streetlight")
                            }
                        }
                        action(AccountRoute, modify(Height6, Opacity2)) {
                            icon("empty-profile", modify(Height100))
                        }
                    }
                    box(SinglePageId.portalMount, modify(Width100))
                    // homeContent()
                }
            }
            box(FullscreenId.mount)
        }
        script(src = "https://cdn.jsdelivr.net/npm/maplibre-gl@5.12.0/dist/maplibre-gl.js") { }
        script(src = "https://cdn.jsdelivr.net/npm/protobufjs/dist/protobuf.min.js") { }
        scripts("launchApp.js")
    }
}

fun FlowContent.appFooter() {
    val giants = "May we choose a world of good and faithful giants. "
    row(modify(AlignItemsCenter)) {
        style = "height: 20rem;"
        column(modify(AlignItemsCenter, Gap0, Width100)) {
            lottie("spinning_circles") {
                style = "height: 10rem;"
            }
            paragraph(giants, modify(Italic, Opacity6))
        }
    }
}

object SinglePageId {
    val viewportBox = Id("viewport-box")
    val app = Id("app")
    val portalMount = Id("portal-mount")
}