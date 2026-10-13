package koala.dom.component

import kampfire.model.Url
import koala.modifier.*
import koala.component.heading5
import koala.component.image
import koala.component.textBlock
import kotlinx.css.pct
import kotlinx.html.DIV
import kotlinx.html.js.div
import web.html.HTMLElement
import koala.dom.AppendScope
import koala.dom.asWeb
import koala.dom.onClick

/** A container with a card's background and corners. */
fun AppendScope.card(
    mod: Modifier? = null,
    content: DIV.() -> Unit = {},
) = div {
    addModifiers(Card, mod)
    content()
}.asWeb()

/** A card showing a thumbnail beside a title and description, calling [onClick] when clicked. */
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