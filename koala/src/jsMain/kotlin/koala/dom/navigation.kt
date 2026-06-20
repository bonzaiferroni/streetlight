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
    text: String = "",
    id: Id? = null,
    flair: String? = null,
    block: A.() -> Unit = {}
) = a {
    configureNavigation(href, modifiers, text, id, flair, block)
}

fun TagScope.navigation(
    route: AppRoute,
    modifiers: ModifierSet? = null,
    text: String = "",
    block: A.() -> Unit = {},
) = navigation(route.toSitePath(), modifiers, text, block = block)