package streetlight.web.dom.layout

import koala.SvgFile
import koala.component.buttonPopover
import koala.component.heading3
import koala.component.heading5
import koala.component.navigationIfNotNull
import koala.component.spacer
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
import streetlight.model.ui.HomeRoute
import streetlight.web.model.toRoute

fun AppendScope.actionSection(
    label: String,
    actions: List<MenuAction>? = null,
    heading: DIV.(String) -> Unit = DIV::defaultSectionHeading,
    content: AppendScope.() -> Unit,
) = section {
    row(modify(BorderBottom2Px, AlignItemsCenter)) {
        heading(label)
        menuActionsRow(actions)
    }
    content()
}

fun DIV.defaultSectionHeading(label: String) = heading3(label, modify(OpacityHigh, Bold))

/** An [actionSection] in a card with a small heading, as a form shows a list the user adds to and edits. */
fun AppendScope.actionSectionCard(
    label: String,
    actions: List<MenuAction>? = null,
    content: AppendScope.() -> Unit,
) = card(Padding(2)) {
    actionSection(label, actions, heading = { heading5(it) }, content = content)
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
        textBlock(action.label, modify(TextUppercase, TextSmall, OpacityHigh, Bold, PaddingX1))
    }
}