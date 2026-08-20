package koala.dom

import koala.css.*
import koala.html.AppRoute
import koala.html.Id
import koala.html.configureNavigation
import kotlinx.html.A
import kotlinx.html.a

// td: share code with common source set
fun AppendScope.navigation(
    href: String? = null,
    modifiers: ModifierSet? = null,
    id: Id? = null,
    flair: String? = null,
    block: A.() -> Unit = {}
) = a {
    configureNavigation(href, modifiers, id, flair, block)
}

fun AppendScope.navigation(
    route: AppRoute,
    modifiers: ModifierSet? = null,
    block: A.() -> Unit = {},
) = navigation(route.toRelativePath(), modifiers, block = block)