package streetlight.web.dom.layout

import koala.Svg
import koala.component.heading5
import koala.dom.AppendScope
import koala.dom.component.card
import koala.model.MenuAction
import koala.modifier.ModifierSet

/**
 * A card headed by [label] in a small heading, with [svg] before it and its [actions] at the end of that row, above
 * its [content], as a form shows a list the user adds to and edits.
 */
fun AppendScope.actionCard(
    label: String,
    actions: List<MenuAction>? = null,
    mod: ModifierSet? = null,
    svg: Svg? = null,
    content: AppendScope.() -> Unit,
) = card(mod) {
    actionHeading(label, actions, heading = { heading5(it) }, svg = svg)
    content()
}
