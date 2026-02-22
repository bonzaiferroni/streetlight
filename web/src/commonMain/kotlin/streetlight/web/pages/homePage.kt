package streetlight.web.pages

import koala.html.*
import kotlinx.html.*
import koala.css.*
import streetlight.model.data.Event
import streetlight.web.shells.homeShell

fun HTML.homePage(events: List<Event>) {
    head("Streetlight | Home") {
        supportProtobuf()
        supportGeoMap()
    }
    appBody {
        homeShell(events)
    }
}

fun FlowContent.appFooter() {
    val giants = "May we choose a world of good and faithful giants. "
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