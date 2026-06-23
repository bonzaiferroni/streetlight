package koala.dom

import koala.css.*
import koala.html.ActionKey
import koala.html.AppRoute
import koala.html.Id
import koala.html.configureNavigation
import kotlinx.html.A
import kotlinx.html.a

// td: share code with common source set
fun TagScope.navigation(
    href: String? = null,
    modifiers: ModifierSet? = null,
    id: Id? = null,
    flair: String? = null,
    block: A.() -> Unit = {}
) = a {
    configureNavigation(href, modifiers, id, flair, block)
}

fun TagScope.navigation(
    route: AppRoute,
    modifiers: ModifierSet? = null,
    block: A.() -> Unit = {},
) = navigation(route.toSitePath(), modifiers, block = block)