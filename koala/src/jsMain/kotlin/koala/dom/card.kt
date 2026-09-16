package koala.dom

import kampfire.model.Url
import koala.modifier.AlignItemsStart
import koala.modifier.BorderRadius1
import koala.modifier.Card
import koala.modifier.Flex1
import koala.modifier.Gap0
import koala.modifier.Height100Pct
import koala.modifier.Height8
import koala.modifier.ModifierSet
import koala.modifier.OverflowHidden
import koala.modifier.Aspect1
import koala.modifier.addModifiers
import koala.modifier.modify
import koala.html.column
import koala.html.heading5
import koala.html.image
import koala.html.row
import koala.html.textBlock
import kotlinx.html.DIV
import kotlinx.html.js.div
import web.html.HTMLElement

fun AppendScope.card(
    mod: ModifierSet? = null,
    content: DIV.() -> Unit = {},
) = div {
    addModifiers(Card, mod)
    content()
}.asWeb()

fun AppendScope.cardOf(
    title: String,
    thumbUrl: Url?,
    description: String?,
    mod: ModifierSet? = null,
    onClick: (() -> Unit)? = null,
): HTMLElement {
    val element = card(mod) {
        row(modify(Height8, AlignItemsStart)) {
            thumbUrl?.let {
                image(thumbUrl, modify(Height100Pct, Aspect1, BorderRadius1))
            }
            column(modify(Flex1, Gap0, Height100Pct)) {
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