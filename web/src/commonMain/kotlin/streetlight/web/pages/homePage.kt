package streetlight.web.pages

import koala.html.*
import kotlinx.html.*
import koala.css.*
import streetlight.model.data.Event
import streetlight.model.data.Location
import streetlight.web.shells.SpotlightContent
import streetlight.web.shells.homeShell

fun HTML.homePage(content: SpotlightContent) {
    head("Streetlight | Home") {
        supportProtobuf()
        supportGeoMap()
    }
    appBody {
        homeShell(content)
    }
}

fun FlowContent.appFooter() {
    val giants = "May we build a world of faithful giants. "
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