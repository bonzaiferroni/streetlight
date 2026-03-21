package streetlight.web.pages

import koala.html.*
import kotlinx.html.*
import koala.css.*
import streetlight.web.shells.HomeContent
import streetlight.web.shells.homeShell

fun HTML.homePage(content: HomeContent) {
    head("Streetlight | Home") {
        supportProtobuf()
        supportGeoMap()
    }
    appBody {
        homeShell(content)
    }
}

fun FlowContent.appFooter() {
    val giants = "May we build us a world of faithful giants."
    row(modify(JustifyCenter)) {
        style = "height: 20rem;"
        column(modify(AlignItemsCenter, Gap0, Width100)) {
            lottie("spinning_circles") {
                style = "height: 10rem;"
            }
            textBlock(giants, modify(Italic, Opacity6))
        }
    }
}