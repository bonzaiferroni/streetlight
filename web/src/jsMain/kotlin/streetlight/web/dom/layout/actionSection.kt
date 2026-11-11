package streetlight.web.dom.layout

import koala.Svg
import koala.SvgFile
import koala.component.buttonPopover
import koala.component.heading3
import koala.component.heading5
import koala.component.navigationIfNotNull
import koala.component.textBlock
import koala.dom.AppendScope
import koala.dom.component.*
import koala.model.MenuAction
import koala.modifier.*
import koala.modifier.invoke
import kampfire.model.Url
import koala.model.WebRoute
import kotlinx.html.DIV
import streetlight.model.data.Entity
import streetlight.web.model.toRoute

/**
 * A section headed by [label], built by [heading], with [svg] before it, and its [actions] at the end of that row,
 * above its [content]. With [onClickIcon], the icon is a button that calls it.
 */
fun AppendScope.actionSection(
    label: String,
    actions: List<MenuAction>? = null,
    heading: DIV.(String) -> Unit = DIV::defaultSectionHeading,
    svg: Svg? = null,
    onClickIcon: (() -> Unit)? = null,
    content: AppendScope.() -> Unit,
) = section {
    row(modify(BorderBottom2Px, AlignItemsCenter)) {
        svg?.let {
            if (onClickIcon == null) icon(it, LargeIconHeight) else button(it, onClickIcon, LargeIconHeight)
        }
        heading(label)
        menuActionsRow(actions)
    }
    content()
}

/** An [actionSection] whose icon is a back arrow that calls [onBack]. */
fun AppendScope.actionSection(
    label: String,
    onBack: () -> Unit,
    actions: List<MenuAction>? = null,
    heading: DIV.(String) -> Unit = DIV::defaultSectionHeading,
    content: AppendScope.() -> Unit,
) = actionSection(label, actions, heading, SvgFile.ArrowLeft.large, onBack, content)

fun DIV.defaultSectionHeading(label: String) = heading3(label, modify(OpacityHigh, Bold))

/** An [actionSection] in a card with a small heading, as a form shows a list the user adds to and edits. */
fun AppendScope.actionSectionCard(
    label: String,
    actions: List<MenuAction>? = null,
    svg: Svg? = null,
    content: AppendScope.() -> Unit,
) = card(Padding(2)) {
    actionSection(label, actions, heading = { heading5(it) }, svg = svg, content = content)
}

fun AppendScope.actionRow(
    entity: Entity,
    actions: List<MenuAction>? = null,
    popoverActions: List<MenuAction>? = null
) = actionRow(entity.label, entity.image?.thumb, entity.toRoute(), actions, popoverActions)

/** A row showing the thumb at [imageUrl] and [label], linked to [route] when given, beside its actions. */
fun AppendScope.actionRow(
    label: String,
    imageUrl: Url? = null,
    route: WebRoute? = null,
    actions: List<MenuAction>? = null,
    popoverActions: List<MenuAction>? = null
) {
    row(modify(AlignItemsCenter, ZenBg, Padding(1))) {
        image(imageUrl, modify(Height(5), BorderRadius1, OverflowClip))
        navigationIfNotNull(route) {
            heading5(label)
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