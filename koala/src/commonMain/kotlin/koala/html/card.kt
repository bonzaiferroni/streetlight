package koala.html

import kampfire.model.Url
import koala.modifier.*
import kotlinx.css.pct
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
    card(modify(Width(100.pct), modifiers)) {
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