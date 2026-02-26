package koala.html

import kotlinx.html.*
import koala.css.*

fun FlowContent.logo(
    modifiers: ModifierSet? = null
) {
    box(modify(ElementClass.logo, modifiers))
}