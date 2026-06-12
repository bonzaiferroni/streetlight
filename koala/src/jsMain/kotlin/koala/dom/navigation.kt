package koala.dom

import koala.css.*
import koala.html.ActionKey
import koala.html.AppRoute
import kotlinx.html.A
import kotlinx.html.a

// td: share code with common source set
fun AppScope.navigation(
    modifiers: ModifierSet? = null,
    block: (A.() -> Unit)? = null
) = a {
    addModifiers(ActionKey.Class, modifiers)
    block?.invoke(this)
}

fun AppScope.navigation(
    route: AppRoute,
    modifiers: ModifierSet? = null,
    block: (A.() -> Unit)? = null
) = navigation(modifiers = modifiers) {
    href = route.toSitePath()
    block?.invoke(this)
}