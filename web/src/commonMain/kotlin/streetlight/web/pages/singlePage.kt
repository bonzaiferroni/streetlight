package streetlight.web.pages

import koala.html.*
import kotlinx.html.*
import koala.css.*

fun HTML.singlePage() {
    head("Streetlight | Home") {
        supportProtobuf()
        supportGeoMap()
    }
    portalBody()
}

fun FlowContent.appFooter() {
    val giants = "May we choose a world of good and faithful giants. "
    row(modify(AlignItemsCenter)) {
        style = "height: 20rem;"
        column(modify(AlignItemsCenter, Gap0, Width100)) {
            lottie("spinning_circles") {
                style = "height: 10rem;"
            }
            textBlock(giants, modify(Italic, Opacity6))
        }
    }
}

object SinglePageId {
    val viewportBox = Id("viewport-box")
    val app = Id("app")
    val portalMount = Id("portal-mount")
}