package koala.html

import koala.css.AlignItemsStart
import koala.css.BorderRadius1
import koala.css.Card
import koala.css.Flex1
import koala.css.Gap0
import koala.css.Height100
import koala.css.Height8
import koala.css.ModifierSet
import koala.css.OverflowHidden
import koala.css.Square
import koala.css.Width100
import koala.css.addModifiers
import koala.css.modify
import kotlinx.html.DIV

import kotlinx.html.FlowContent
import kotlinx.html.div

inline fun FlowContent.card(
    modifiers: ModifierSet? = null,
    crossinline content: DIV.() -> Unit,
) {
    div {
        addModifiers(Card, modifiers)
        content()
    }
}

fun FlowContent.cardOf(
    title: String,
    thumbUrl: String?,
    description: String?,
    modifiers: ModifierSet? = null,
) {
    card(modify(Width100, modifiers)) {
        row(modify(Height8, AlignItemsStart)) {
            thumbUrl?.let {
                image(thumbUrl, modify(Height100, Square, BorderRadius1))
            }
            column(modify(Flex1, Gap0, Height100)) {
                heading5(title)
                description?.let {
                    textBlock(description, modify(Flex1, OverflowHidden))
                }
            }
        }
    }
}

fun FlowContent.cardOf(
    route: AppRoute?,
    title: String,
    thumbUrl: String?,
    description: String?,
    modifiers: ModifierSet? = null,
) {
    if (route != null) {
        action(route, modifiers) {
            cardOf(title, thumbUrl, description)
        }
    } else {
        cardOf(title, thumbUrl, description, modifiers)
    }
}