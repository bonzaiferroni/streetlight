package streetlight.web.dom.layout

import koala.Svg
import koala.dom.AppendScope
import koala.dom.component.card
import koala.model.HeadingLevel
import koala.model.MenuAction
import koala.modifier.ModifierSet
import koala.modifier.modify
import koala.modifier.transition

/**
 * A card headed by [label] at [level], with [svg] before it and its [actions] at the end of that row, above its
 * [content], as a form shows a list the user adds to and edits.
 *
 * A [key], unique on the page and free of spaces, names the card `card-` and the key for view transitions, and its
 * heading as [actionHeading] does.
 */
fun AppendScope.actionCard(
    label: String,
    actions: List<MenuAction>? = null,
    mod: ModifierSet? = null,
    svg: Svg? = null,
    level: HeadingLevel = HeadingLevel.H5,
    key: String? = null,
    content: AppendScope.() -> Unit,
) = card(modify(mod, key?.let { transition("card-$it") })) {
    actionHeading(label, actions, level = level, svg = svg, key = key)
    content()
}
