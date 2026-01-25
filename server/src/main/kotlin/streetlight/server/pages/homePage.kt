package streetlight.server.pages

import koala.html.*
import kotlinx.html.*

fun HTML.homePage() {
    head("Streetlight | Home") {
        styles("homePage.css")
        scripts("launchApp.js")
        geoMapResources()
        script(src = "https://cdn.jsdelivr.net/npm/protobufjs/dist/protobuf.min.js") { }
    }
    body {
        column {
            column(FillWidth, AlignItemsCenter, NoGap) {
                row {
                    heading1("Streetlight")
                    logo()
                }
                box(Dim) {
                    +"a "
                    span {
                        modify(NoDim, Glow)
                        +"Colfax"
                    }
                    +" music community"
                }
                box(Id("app-navigator"))
            }
        }
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