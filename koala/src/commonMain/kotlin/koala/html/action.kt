package koala.html

import koala.css.ElementClass
import koala.css.ModifierSet
import koala.css.StyleSet
import koala.css.applyModifiers
import koala.css.applyStyles
import koala.css.modify
import kotlinx.html.A
import kotlinx.html.FlowContent
import kotlinx.html.FlowOrInteractiveOrPhrasingContent
import kotlinx.html.a

fun FlowContent.action(
    href: String? = null,
    modifiers: ModifierSet? = null,
    text: String = "",
    id: Id? = null,
    styles: StyleSet? = null,
    block: (A.() -> Unit)? = null
) {
    a {
        applyId(id)
        applyModifiers(modify(ElementClass.action, modifiers))
        applyStyles(styles)
        href?.let { this.href = href }
        block?.invoke(this)
        +text
    }
}

fun FlowContent.action(
    route: AppRoute,
    modifiers: ModifierSet? = null,
    text: String = "",
    id: Id? = null,
    styles: StyleSet? = null,
    block: (A.() -> Unit)? = null
) {
    action(
        text = text,
        href = route.toHashPath(),
        modifiers = modifiers,
        id = id,
        styles = styles,
        block = block
    )
}

fun FlowContent.actionIfNotNull(
    href: String? = null,
    text: String = "",
    modifiers: ModifierSet? = null,
    id: Id? = null,
    block: (FlowContent.() -> Unit)? = null
) {
    if (href == null) {
        block?.invoke(this)
    } else {
        action(href = href, text = text, modifiers = modifiers, id = id, block = block)
    }
}