package streetlight.web.dom.layout

import koala.Svg
import koala.SvgFile
import koala.dom.AppendScope
import koala.dom.component.card
import koala.model.HeadingLevel
import koala.model.IconAction
import koala.model.MenuAction
import koala.modifier.Modifier
import koala.modifier.ModifierSet
import koala.modifier.modify
import koala.modifier.transition

/**
 * A card headed by [label] at [level], with [svg] before it and its [actions] at the end of that row, then its
 * [icons], above its [content], as a form shows a list the user adds to and edits.
 *
 * With [onBack], the card is the focus of a layout, shown in place of its overview: an up arrow ends its heading row,
 * as tall as the row, and calls it to release the focus.
 *
 * A [key], unique on the page and free of spaces, names the card `card-` and the key for view transitions. Its
 * heading takes no name of its own, and travels with the card.
 */
fun AppendScope.actionCard(
    label: String,
    actions: List<MenuAction>? = null,
    mod: Modifier? = null,
    svg: Svg? = null,
    level: HeadingLevel = HeadingLevel.H5,
    key: String? = null,
    icons: List<IconAction>? = null,
    onBack: (() -> Unit)? = null,
    content: AppendScope.() -> Unit,
) = card(modify(mod, key?.let { transition("card-$it") })) {
    val trailIcon = onBack?.let { IconAction(SvgFile.ArrowUp.large, "Back", onClick = it) }
    actionHeading(
        label = label,
        actions = actions,
        level = level,
        svg = svg,
        icons = icons,
        trailIcon = trailIcon,
    )
    content()
}
