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
    id: Id? = null,
    flair: String? = null,
    block: A.() -> Unit = {}
) {
    a {
        configureNavigation(href, modifiers, id, flair, block)
    }
}

internal fun A.configureNavigation(
    href: String? = null,
    modifiers: ModifierSet? = null,
    id: Id? = null,
    flair: String? = null,
    block: A.() -> Unit = {}
) {
    setId(id)
    addModifiers(modify(ActionKey.Class, modifiers))
    href?.let { this.href = href }
    flair?.let {
        span {
            +flair
        }
    }
    block()
}

fun FlowContent.navigation(
    route: AppRoute,
    modifiers: ModifierSet? = null,
    id: Id? = null,
    flair: String? = null,
    block: A.() -> Unit = {}
) {
    navigation(
        href = route.toRelativePath(),
        modifiers = modifiers,
        id = id,
        flair = flair,
        block = block
    )
}

fun FlowContent.navigationIfNotNull(
    route: AppRoute? = null,
    modifiers: ModifierSet? = null,
    id: Id? = null,
    block: FlowContent.() -> Unit = {}
) {
    navigationIfNotNull(route?.toRelativePath(), modifiers, id, block)
}

fun FlowContent.navigationIfNotNull(
    href: String? = null,
    modifiers: ModifierSet? = null,
    id: Id? = null,
    block: FlowContent.() -> Unit = {}
) {
    if (href == null) {
        box(id, modifiers) {
            block()
        }
    } else {
        navigation(href = href, modifiers = modifiers, id = id, block = block)
    }
}

fun FlowContent.navigation(
    targetId: Id,
    modifiers: ModifierSet? = null,
    block: A.() -> Unit = {}
) {
    a {
        addModifiers(modifiers)
        onClick = KoalaFun.ScrollToId.invoke(targetId)
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