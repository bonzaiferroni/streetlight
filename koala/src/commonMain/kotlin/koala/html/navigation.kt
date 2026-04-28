package koala.html

import koala.css.Class
import koala.css.KoalaFun
import koala.css.ModifierSet
import koala.css.addModifiers
import koala.css.modify
import kotlinx.html.A
import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.onClick

fun FlowContent.navigation(
    href: String? = null,
    modifiers: ModifierSet? = null,
    text: String = "",
    id: Id? = null,
    flair: String? = null,
    block: A.() -> Unit = {}
) {
    a {
        setId(id)
        addModifiers(modify(ActionKey.Class, modifiers))
        href?.let { this.href = href }
        block()
        flair?.let {
            span {
                +flair
            }
        }
        span {
            +text
        }
    }
}

fun FlowContent.navigation(
    route: AppRoute,
    modifiers: ModifierSet? = null,
    text: String = "",
    id: Id? = null,
    flair: String? = null,
    block: A.() -> Unit = {}
) {
    navigation(
        text = text,
        href = route.toSitePath(),
        modifiers = modifiers,
        id = id,
        flair = flair,
        block = block
    )
}

fun FlowContent.navigationIfNotNull(
    route: AppRoute? = null,
    text: String = "",
    modifiers: ModifierSet? = null,
    id: Id? = null,
    block: FlowContent.() -> Unit = {}
) {
    navigationIfNotNull(route?.toSitePath(), text, modifiers, id, block)
}

fun FlowContent.navigationIfNotNull(
    href: String? = null,
    text: String = "",
    modifiers: ModifierSet? = null,
    id: Id? = null,
    block: FlowContent.() -> Unit = {}
) {
    if (href == null) {
        block()
    } else {
        navigation(href = href, text = text, modifiers = modifiers, id = id, block = block)
    }
}

fun FlowContent.navigation(
    targetId: Id,
    modifiers: ModifierSet? = null,
    block: A.() -> Unit = {}
) {
    a {
        addModifiers(modifiers)
        onClick = KoalaFun.ScrollToId.invoke(targetId.arg)
        block()
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