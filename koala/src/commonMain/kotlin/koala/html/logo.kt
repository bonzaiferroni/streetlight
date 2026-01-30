package koala.html

import kotlinx.html.*
import koala.css.*

fun FlowContent.logo(heightRem: Float = 4f) {
    box(LogoClass) {
        style = "height: ${heightRem}rem;"
    }
}