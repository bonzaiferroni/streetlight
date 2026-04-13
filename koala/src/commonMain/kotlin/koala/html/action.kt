package koala.html

import koala.css.Class
import koala.css.ModifierSet
import koala.css.StyleSet
import koala.css.addModifiers
import koala.css.setStyle
import koala.css.modify
import kotlinx.html.A
import kotlinx.html.FlowContent
import kotlinx.html.a

fun FlowContent.navigation(
    href: String? = null,
    modifiers: ModifierSet? = null,
    text: String = "",
    id: Id? = null,
    styles: StyleSet? = null,
    block: (A.() -> Unit)? = null
) {
    a {
        setId(id)
        addModifiers(modify(ActionKey.Class, modifiers))
        setStyle(styles)
        href?.let { this.href = href }
        block?.invoke(this)
        +text
    }
}

fun FlowContent.navigation(
    route: AppRoute,
    modifiers: ModifierSet? = null,
    text: String = "",
    id: Id? = null,
    styles: StyleSet? = null,
    block: (A.() -> Unit)? = null
) {
    navigation(
        text = text,
        href = route.toHashPath(),
        modifiers = modifiers,
        id = id,
        styles = styles,
        block = block
    )
}

fun FlowContent.navigationIfNotNull(
    href: String? = null,
    text: String = "",
    modifiers: ModifierSet? = null,
    id: Id? = null,
    block: (FlowContent.() -> Unit)? = null
) {
    if (href == null) {
        block?.invoke(this)
    } else {
        navigation(href = href, text = text, modifiers = modifiers, id = id, block = block)
    }
}

object ActionKey {
    val Class = Class("action")
}

// language="CSS"
val ActionCss get() = """
.action {
    /*transition: opacity 0.35s ease;*/
    cursor: pointer;
}

.action:hover {
    /*opacity: 1;*/
}
"""