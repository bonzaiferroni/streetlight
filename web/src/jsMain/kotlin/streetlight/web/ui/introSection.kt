package streetlight.web.ui

import koala.Lottie
import koala.modifier.*
import koala.dom.*
import koala.component.filigree
import koala.component.heading1
import kotlinx.html.DIV
import koala.dom.component.*
import koala.dom.layout.*

@Deprecated("use body component")
fun ViewScope.introSection(
    name: String,
    mod: Modifier? = null,
    lottie: Lottie? = null,
    block: DIV.() -> Unit
) {
    section(mod) {
        filigree {
            heading1(name, modify(Shrinkable, AntiShadow))
        }
        row(AlignItemsCenter) {
            column(modify(Flex4, PaddingLeft(3))) {
                block()
            }
            lottie?.let {
                row(modify(Flex1, JustifyContentCenter)) {
                    lottie(it, MaxHeight(32))
                }
            }
        }
    }
}