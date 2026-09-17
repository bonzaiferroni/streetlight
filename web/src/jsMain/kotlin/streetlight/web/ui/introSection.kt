package streetlight.web.ui

import koala.Lottie
import koala.modifier.*
import koala.dom.*
import koala.html.filigree
import koala.html.heading1
import kotlinx.html.DIV

fun ViewScope.introSection(
    name: String,
    mod: ModifierSet? = null,
    lottie: Lottie? = null,
    block: DIV.() -> Unit
) {
    section(mod) {
        filigree {
            heading1(name, modify(Shrinkable, AntiShadow))
        }
        row(modify(AlignItemsCenter)) {
            column(modify(Flex4, FlexMd2, PaddingLeft3)) {
                block()
            }
            lottie?.let {
                row(modify(Flex1, JustifyContentCenter)) {
                    lottie(it, modify(MaxHeight(32)))
                }
            }
        }
    }
}