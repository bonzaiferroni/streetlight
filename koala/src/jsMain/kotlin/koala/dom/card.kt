package koala.dom

import kampfire.model.Url
import koala.modifier.*
import koala.html.column
import koala.html.heading5
import koala.html.image
import koala.html.row
import koala.html.textBlock
import kotlinx.css.pct
import kotlinx.html.DIV
import kotlinx.html.js.div
import web.html.HTMLElement

fun AppendScope.card(
    mod: Modifier? = null,
    content: DIV.() -> Unit = {},
) = div {
    addModifiers(Card, mod)
    content()
}.asWeb()

fun AppendScope.cardOf(
    title: String,
    thumbUrl: Url?,
    description: String?,
    mod: Modifier? = null,
    onClick: (() -> Unit)? = null,
): HTMLElement {
    val element = card(mod) {
        row(modify(Height(8), AlignItemsStart)) {
            thumbUrl?.let {
                image(thumbUrl, modify(Height(100.pct), Aspect1, BorderRadius1))
            }
            column(modify(Flex1, Gap(0), Height(100.pct))) {
                heading5(title)
                description?.let {
                    textBlock(description, modify(Flex1, OverflowHidden))
                }
            }
        }
    }

    onClick?.let { onClick ->
        element.onClick {
            onClick()
        }
    }

    return element
}