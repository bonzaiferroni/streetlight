package koala.dom

import koala.modifier.*
import koala.html.AppRoute
import koala.html.Id
import koala.html.configureNavigation
import kotlinx.html.A
import kotlinx.html.a

// td: share code with common source set
/** A link to [href], with an optional [flair] before its content. */
fun AppendScope.navigation(
    href: String? = null,
    mod: Modifier? = null,
    id: Id? = null,
    flair: String? = null,
    block: A.() -> Unit = {}
) = a {
    configureNavigation(href, mod, id, flair, block)
}

/** A link to [route]. */
fun AppendScope.navigation(
    route: AppRoute,
    mod: Modifier? = null,
    block: A.() -> Unit = {},
) = navigation(route.toRelativePath(), mod, block = block)

/** A text link to [href] in the primary color. */
fun AppendScope.navigation(
    label: String,
    href: String,
    mod: Modifier? = null,
    config: A.() -> Unit = {}
) = navigation(href, modify(mod, PrimaryFg)) {
    config()
    +label
}