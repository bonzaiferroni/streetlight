package koala.html

import koala.modifier.Class
import koala.interop.KoalaFun
import koala.modifier.Modifier
import koala.modifier.PrimaryFg
import koala.modifier.addModifiers
import koala.modifier.modify
import kotlinx.html.A
import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.onClick

fun FlowContent.navigation(
    href: String? = null,
    mod: Modifier? = null,
    id: Id? = null,
    flair: String? = null,
    config: A.() -> Unit = {},
) {
    a {
        configureNavigation(href, mod, id, flair, config)
    }
}

internal fun A.configureNavigation(
    href: String? = null,
    mod: Modifier? = null,
    id: Id? = null,
    flair: String? = null,
    config: A.() -> Unit = {}
) {
    setId(id)
    addModifiers(modify(ActionKey.Class, mod))
    href?.let { this.href = href }
    flair?.let {
        span {
            +flair
        }
    }
    config()
}

fun FlowContent.navigation(
    text: String,
    href: String,
    mod: Modifier? = null,
    config: A.() -> Unit = { }
) {
    navigation(href, modify(mod, PrimaryFg)) {
        config()
        +text
    }
}

fun FlowContent.navigation(
    route: AppRoute,
    mod: Modifier? = null,
    id: Id? = null,
    flair: String? = null,
    block: A.() -> Unit = {}
) {
    navigation(
        href = route.toRelativePath(),
        mod = mod,
        id = id,
        flair = flair,
        config = block
    )
}

fun FlowContent.navigationIfNotNull(
    route: AppRoute? = null,
    mod: Modifier? = null,
    id: Id? = null,
    block: FlowContent.() -> Unit = {}
) {
    navigationIfNotNull(route?.toRelativePath(), mod, id, block)
}

fun FlowContent.navigationIfNotNull(
    href: String? = null,
    mod: Modifier? = null,
    id: Id? = null,
    block: FlowContent.() -> Unit = {}
) {
    if (href == null) {
        box(id, mod) {
            block()
        }
    } else {
        navigation(href = href, mod = mod, id = id, config = block)
    }
}

fun FlowContent.navigation(
    targetId: Id,
    mod: Modifier? = null,
    block: A.() -> Unit = {}
) {
    a {
        addModifiers(mod)
        onClick = KoalaFun.ScrollToId.invokeJs(targetId)
        block()
    }
}

object ActionKey {
    val Class = Class("action")
}

// language="CSS"
val ActionCss get() = with(ActionKey) { """
$Class {
    /*transition: opacity 0.35s ease;*/
    cursor: pointer;
}

$Class:hover {
    /*opacity: 1;*/
}
""" }