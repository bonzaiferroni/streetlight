package streetlight.web.dom.layout

import koala.Svg
import koala.SvgFile
import koala.component.buttonPopover
import koala.component.heading
import koala.component.heading5
import koala.component.navigationIfNotNull
import koala.component.textBlock
import koala.dom.AppendScope
import koala.dom.component.*
import koala.model.HeadingLevel
import koala.model.MenuAction
import koala.modifier.*
import koala.modifier.invoke
import kampfire.model.Url
import koala.model.WebRoute
import streetlight.model.data.Entity
import streetlight.web.model.toRoute

/**
 * A section headed by [label] at [level], modified by [headingMod], with [svg] before it, and its [actions] at the end
 * of that row, above its [content]. With [onClickIcon], the icon is a button that calls it. A [key] names its heading
 * for view transitions, as [actionHeading] does.
 */
fun AppendScope.actionSection(
    label: String,
    actions: List<MenuAction>? = null,
    level: HeadingLevel = HeadingLevel.H3,
    svg: Svg? = null,
    onClickIcon: (() -> Unit)? = null,
    headingMod: Modifier? = null,
    key: String? = null,
    content: AppendScope.() -> Unit,
) = section {
    actionHeading(label, actions, BorderBottom2Px, level, svg, onClickIcon, headingMod, key)
    content()
}

/**
 * A row headed by [label] at [level], modified by [headingMod], with [svg] before it and its [actions] at its end.
 * With [onClickIcon], the icon is a button that calls it.
 *
 * The row is as tall as its level, and its icon fills that height. A [key], unique on the page and free of spaces,
 * names the heading `heading-` and the key for view transitions.
 */
fun AppendScope.actionHeading(
    label: String,
    actions: List<MenuAction>? = null,
    mod: Modifier? = null,
    level: HeadingLevel = HeadingLevel.H3,
    svg: Svg? = null,
    onClickIcon: (() -> Unit)? = null,
    headingMod: Modifier? = null,
    key: String? = null,
) = row(modify(mod, AlignItemsCenter, Height(level.rowUnits))) {
    svg?.let {
        if (onClickIcon == null) icon(it, Height100Pct) else button(it, onClickIcon, Height100Pct)
    }
    heading(level, label, modify(level.headingMod, headingMod, key?.let { transition("heading-$it") }))
    menuActionsRow(actions)
}

/** An [actionSection] whose icon is a back arrow that calls [onBack]. */
fun AppendScope.actionSection(
    label: String,
    onBack: () -> Unit,
    actions: List<MenuAction>? = null,
    level: HeadingLevel = HeadingLevel.H3,
    headingMod: Modifier? = null,
    key: String? = null,
    content: AppendScope.() -> Unit,
) = actionSection(label, actions, level, SvgFile.ArrowLeft.large, onBack, headingMod, key, content)

/** The height of an action heading's row at this level, in units, near the height of its text. */
private val HeadingLevel.rowUnits get() = when (this) {
    HeadingLevel.H1 -> 10
    HeadingLevel.H2 -> 7
    HeadingLevel.H3 -> 5
    HeadingLevel.H4 -> 4
    HeadingLevel.H5 -> 3
    HeadingLevel.H6 -> 2
}

/** The look of an action heading at this level: the thin weights of the larger levels are made firmer and dimmed. */
private val HeadingLevel.headingMod get() = when (this) {
    HeadingLevel.H5, HeadingLevel.H6 -> null
    else -> modify(OpacityHigh, Bold)
}

fun AppendScope.actionRow(
    entity: Entity,
    actions: List<MenuAction>? = null,
    popoverActions: List<MenuAction>? = null
) = actionRow(entity.label, entity.image?.thumb, entity.toRoute(), actions, popoverActions)

/**
 * A row showing the thumb at [imageUrl] and [label], modified by [labelMod], linked to [route] when given, beside its
 * actions.
 */
fun AppendScope.actionRow(
    label: String,
    imageUrl: Url? = null,
    route: WebRoute? = null,
    actions: List<MenuAction>? = null,
    popoverActions: List<MenuAction>? = null,
    labelMod: Modifier? = null,
) {
    row(modify(AlignItemsCenter, ZenBg, Padding(1))) {
        image(imageUrl, modify(Height(5), BorderRadius1, OverflowClip))
        navigationIfNotNull(route) {
            heading5(label, labelMod)
        }
        menuActionsRow(actions)
        popoverActions?.let {
            buttonPopover(SvgFile.Dots.filled, Height(3)) {
                textBlock("hello actions")
            }
        }
    }
}

fun AppendScope.menuActionsRow(actions: List<MenuAction>?) {
    row(modify(Flex1, JustifyContentEnd)) {
        actions?.forEach {
            menuActionButton(it)
        }
    }
}

fun AppendScope.menuActionButton(action: MenuAction) {
    button(action.onClick) {
        action.config(this)
        textBlock(action.label, modify(TextUppercase, TextSmall, OpacityHigh, Bold, PaddingX(1)))
    }
}

fun AppendScope.actionMenu(actions: List<MenuAction>) {
    column(Gap0) {
        actions.forEach { option ->
            popoverOption(option)
        }
    }
}