package streetlight.web.pages

import koala.html.*
import kotlinx.html.*
import koala.css.*
import streetlight.web.AccountRoute
import streetlight.web.HomeRoute

fun HTML.homePage() {
    head("Streetlight | Home") {
        styles("homePage.css")
        link(href = "https://cdn.jsdelivr.net/npm/maplibre-gl@5.12.0/dist/maplibre-gl.css", "stylesheet")
        koalaStyles()
    }
    body {
        column {
            column(modify(Width100, AlignItemsCenter)) {
                row(modify(Width100, SpaceBetween)) {
                    action(modify(Height3, Opacity2)) {
                        icon("chevron-down", modify(Height100))
                    }
                    action(HomeRoute()) {
                        row {
                            logo()
                            heading1("Streetlight")
                        }
                    }
                    action(AccountRoute, modify(Height3, Opacity2)) {
                        icon("empty-profile", modify(Height100))
                    }
                }
                div {
                    id = "portal-mount"
                    style = "width: 100%;"
                }
                // homeContent()
            }
        }
        script(src = "https://cdn.jsdelivr.net/npm/maplibre-gl@5.12.0/dist/maplibre-gl.js") { }
        script(src = "https://cdn.jsdelivr.net/npm/protobufjs/dist/protobuf.min.js") { }
        scripts("launchApp.js")
    }
}

fun FlowContent.homeFooter() {
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