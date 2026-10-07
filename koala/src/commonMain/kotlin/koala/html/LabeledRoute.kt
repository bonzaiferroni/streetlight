package koala.html

import kampfire.model.Labeled

/** An [AppRoute] with the [label] that links to it. */
data class LabeledRoute(
    val route: AppRoute,
    override val label: String = route.title,
): Labeled

/** This route with [label] as its link text, or its title when [label] is `null`. */
fun AppRoute.withLabel(label: String? = null) = LabeledRoute(this, label ?: title)
