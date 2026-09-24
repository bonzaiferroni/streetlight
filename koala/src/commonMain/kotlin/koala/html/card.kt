package koala.html

import kampfire.model.Url
import koala.modifier.*
import kotlinx.css.pct
import kotlinx.html.DIV

import kotlinx.html.FlowContent
import kotlinx.html.div

/** A container with a card's background and corners. */
inline fun FlowContent.card(
    mod: Modifier? = null,
    crossinline content: DIV.() -> Unit,
) {
    div {
        addModifiers(Card, mod)
        content()
    }
}

/** A card showing a thumbnail beside a title and description. */
fun FlowContent.cardOf(
    title: String,
    thumbUrl: Url?,
    description: String?,
    mod: Modifier? = null,
) {
    card(modify(Width(100.pct), mod)) {
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

/** A card showing a thumbnail beside a title and description, linking to [route] when it is not `null`. */
fun FlowContent.cardOf(
    route: AppRoute?,
    title: String,
    thumbUrl: Url?,
    description: String?,
    mod: Modifier? = null,
) {
    if (route != null) {
        navigation(route, mod) {
            cardOf(title, thumbUrl, description)
        }
    } else {
        cardOf(title, thumbUrl, description, mod)
    }
}