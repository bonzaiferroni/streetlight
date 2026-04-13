package koala.html

import kampfire.model.Url
import koala.css.*
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
    thumbUrl: Url?,
    description: String?,
    modifiers: ModifierSet? = null,
) {
    card(modify(Width100P, modifiers)) {
        row(modify(Height8, AlignItemsStart)) {
            thumbUrl?.let {
                image(thumbUrl, modify(Height100P, AspectRatio1, BorderRadius1))
            }
            column(modify(Flex1, Gap0, Height100P)) {
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
    thumbUrl: Url?,
    description: String?,
    modifiers: ModifierSet? = null,
) {
    if (route != null) {
        navigation(route, modifiers) {
            cardOf(title, thumbUrl, description)
        }
    } else {
        cardOf(title, thumbUrl, description, modifiers)
    }
}