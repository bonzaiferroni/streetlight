package koala.html

import kotlinx.html.*

fun FlowContent.logo(heightRem: Float = 4f) {
    box(Css("logo")) {
        style = "height: ${heightRem}rem;"
    }
}