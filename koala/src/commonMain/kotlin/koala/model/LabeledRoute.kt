package koala.model

import kampfire.model.Labeled

/** An [WebRoute] with the [label] that links to it. */
data class LabeledRoute(
    val route: WebRoute,
    override val label: String = route.title,
): Labeled

/** This route with [label] as its link text, or its title when [label] is `null`. */
fun WebRoute.withLabel(label: String? = null) = LabeledRoute(this, label ?: title)
