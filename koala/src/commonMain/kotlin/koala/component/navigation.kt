package koala.component

import koala.SvgPack
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
import koala.layout.box
import koala.model.AppRoute
import koala.page.Id
import koala.page.setId

/** A link to [href], with an optional [flair] before its content. */
fun FlowContent.navigation(
    href: String? = null,
    mod: Modifier? = null,
    id: Id? = null,
    flair: SvgPack? = null,
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
    flair: SvgPack? = null,
    config: A.() -> Unit = {}
) {
    setId(id)
    addModifiers(modify(ActionKey.Class, mod))
    href?.let { this.href = href }
    flair?.let { flairIcon(it) }
    config()
}

/** A text link to [href] in the primary color. */
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

/** A link to [route]. */
fun FlowContent.navigation(
    route: AppRoute,
    mod: Modifier? = null,
    id: Id? = null,
    flair: SvgPack? = null,
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

/** A link to [route], or a plain [box] around the same content when it is `null`. */
fun FlowContent.navigationIfNotNull(
    route: AppRoute? = null,
    mod: Modifier? = null,
    id: Id? = null,
    block: FlowContent.() -> Unit = {}
) {
    navigationIfNotNull(route?.toRelativePath(), mod, id, block)
}

/** A link to [href], or a plain [box] around the same content when it is `null`. */
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

/** A link that scrolls to the element with [targetId]. */
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