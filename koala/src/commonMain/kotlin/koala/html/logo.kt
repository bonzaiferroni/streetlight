package koala.html

import kotlinx.html.*
import koala.css.*

fun FlowContent.logo(heightRem: Float = 4f) {
    box(modify(ElementClass.logo)) {
        style = "height: ${heightRem}rem;"
    }
}