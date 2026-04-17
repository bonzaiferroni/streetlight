package koala.dom

import kampfire.model.Url
import koala.css.AlignItemsStart
import koala.css.BorderRadius1
import koala.css.Card
import koala.css.Flex1
import koala.css.Gap0
import koala.css.Height100P
import koala.css.Height8
import koala.css.ModifierSet
import koala.css.OverflowHidden
import koala.css.Aspect1
import koala.css.addModifiers
import koala.css.modify
import koala.html.column
import koala.html.heading5
import koala.html.image
import koala.html.row
import koala.html.textBlock
import kotlinx.html.DIV
import kotlinx.html.js.div
import org.w3c.dom.HTMLElement

inline fun DOMContext.card(
    modifiers: ModifierSet? = null,
    crossinline content: DIV.() -> Unit,
) = div {
    addModifiers(Card, modifiers)
    content()
}

fun DOMContext.cardOf(
    title: String,
    thumbUrl: Url?,
    description: String?,
    modifiers: ModifierSet? = null,
    onClick: (() -> Unit)? = null,
): HTMLElement {
    val element = card(modifiers) {
        row(modify(Height8, AlignItemsStart)) {
            thumbUrl?.let {
                image(thumbUrl, modify(Height100P, Aspect1, BorderRadius1))
            }
            column(modify(Flex1, Gap0, Height100P)) {
                heading5(title)
                description?.let {
                    textBlock(description, modify(Flex1, OverflowHidden))
                }
            }
        }
    }

    onClick?.let {
        element.onClick(it)
    }

    return element
}