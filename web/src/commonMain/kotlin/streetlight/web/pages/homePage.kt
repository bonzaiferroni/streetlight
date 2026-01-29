package streetlight.web.pages

import koala.html.*
import kotlinx.html.*
import koala.css.*

fun HTML.homePage() {
    head("Streetlight | Home") {
        styles("homePage.css")
        geoMapResources()
        script(src = "https://cdn.jsdelivr.net/npm/protobufjs/dist/protobuf.min.js") { }
        koalaStyles()
    }
    body {
        column {
            column(FillWidth, AlignItemsCenter) {
                row(NoGap) {
                    logo()
                    heading1("Streetlight")
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
        column(AlignItemsCenter, NoGap, FillWidth) {
            lottie("spinning_circles") {
                style = "height: 10rem;"
            }
            paragraph(giants, Italic, Dim)
        }
    }
}