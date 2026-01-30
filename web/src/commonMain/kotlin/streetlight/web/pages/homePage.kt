package streetlight.web.pages

import koala.html.*
import kotlinx.html.*
import koala.css.*
import streetlight.web.Account
import streetlight.web.Home

fun HTML.homePage() {
    head("Streetlight | Home") {
        styles("homePage.css")
        geoMapResources()
        script(src = "https://cdn.jsdelivr.net/npm/protobufjs/dist/protobuf.min.js") { }
        koalaStyles()
    }
    body {
        column {
            column(Width100, AlignItemsCenter) {
                row(Width100, SpaceBetween) {
                    action(Height3, Opacity2) {
                        icon("chevron-down", Height100)
                    }
                    action(Home()) {
                        row {
                            logo()
                            heading1("Streetlight")
                        }
                    }
                    action(Account, Height3, Opacity2) {
                        icon("empty-profile", Height100)
                    }
                }
//                box(Dim) {
//                    +"a "
//                    span {
//                        modify(NoDim, Glow)
//                        +"Colfax"
//                    }
//                    +" music community"
//                }
                div {
                    id = "portal-mount"
                    style = "width: 100%;"
                }
                // homeContent()
            }
        }
        scripts("launchApp.js")
    }
}

fun FlowContent.homeFooter() {
    val giants = "May we choose a world of good and faithful giants. "
    row(AlignItemsCenter) {
        style = "height: 20rem;"
        column(AlignItemsCenter, Gap0, Width100) {
            lottie("spinning_circles") {
                style = "height: 10rem;"
            }
            paragraph(giants, Italic, Opacity6)
        }
    }
}