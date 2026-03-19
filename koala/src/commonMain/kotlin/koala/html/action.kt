package koala.html

import koala.css.ElementClass
import koala.css.ModifierSet
import koala.css.applyModifiers
import koala.css.modify
import kotlinx.html.A
import kotlinx.html.FlowContent
import kotlinx.html.FlowOrInteractiveOrPhrasingContent
import kotlinx.html.a

fun FlowOrInteractiveOrPhrasingContent.action(
    route: AppRoute,
    modifiers: ModifierSet? = null,
    id: Id? = null,
    block: (A.() -> Unit)? = null
) {
    action(href = route.toHashPath(), modifiers = modifiers, id = id, block = block)
}

fun FlowOrInteractiveOrPhrasingContent.action(
    href: String? = null,
    text: String = "",
    modifiers: ModifierSet? = null,
    id: Id? = null,
    block: (A.() -> Unit)? = null
) {
    action(modifiers) {
        applyId(id)
        href?.let { this.href = href }
        +text
        block?.invoke(this)
    }
}

fun FlowOrInteractiveOrPhrasingContent.action(
    modifiers: ModifierSet? = null,
    block: (A.() -> Unit)? = null
) {
    a {
        applyModifiers(modify(ElementClass.action, modifiers))
        block?.invoke(this)
    }
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
        action(href, text, modifiers, id, block)
    }
}