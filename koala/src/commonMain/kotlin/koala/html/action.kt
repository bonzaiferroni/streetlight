package koala.html

import koala.css.ActionClass
import koala.css.CssClass
import koala.css.applyModifiers
import kotlinx.html.A
import kotlinx.html.FlowContent
import kotlinx.html.a

fun FlowContent.action(
    route: AppRoute,
    vararg modifiers: CssClass,
    block: (A.() -> Unit)? = null
) {
    action(href = route.toHashPath(), modifiers = modifiers, block = block)
}

fun FlowContent.action(
    href: String? = null,
    vararg modifiers: CssClass,
    block: (A.() -> Unit)? = null
) {
    action(*modifiers) {
        href?.let { this.href = href }
        block?.invoke(this)
    }
}

fun FlowContent.action(
    vararg modifiers: CssClass,
    block: (A.() -> Unit)? = null
) {
    a {
        applyModifiers(ActionClass, *modifiers)
        block?.invoke(this)
    }
}