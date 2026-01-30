package koala.html

import koala.css.ActionClass
import koala.css.CssClass
import koala.css.modify
import kotlinx.html.A
import kotlinx.html.FlowContent
import kotlinx.html.a

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
        modify(ActionClass, *modifiers)
        block?.invoke(this)
    }
}