package streetlight.web.dom.layout

import koala.SvgFile
import koala.component.buttonPopover
import koala.component.heading3
import koala.component.heading4
import koala.component.heading5
import koala.component.spacer
import koala.component.textBlock
import koala.dom.ViewScope
import koala.dom.component.*
import koala.model.MenuAction
import koala.modifier.*
import koala.modifier.invoke
import streetlight.model.data.Entity
import streetlight.model.ui.HomeRoute
import streetlight.web.model.toRoute

fun ViewScope.dashboardHeading(label: String, actions: List<MenuAction>? = null) {
    row(modify(BorderBottom2Px, PaddingRight(1), AlignItemsCenter)) {
        heading3(label, modify(OpacityHigh))
        spacer()
        actions?.forEach {
            dashboardAction(it)
        }
    }
}

fun ViewScope.dashboardEntity(entity: Entity, actions: List<MenuAction>? = null, popoverActions: List<MenuAction>? = null) {
    row(modify(AlignItemsCenter, ZenBg, Padding(1))) {
        image(entity.image?.thumb, modify(Height(5), BorderRadius1, OverflowClip))
        navigation(entity.toRoute() ?: HomeRoute()) {
            heading5(entity.label)
        }
        row(modify(Flex1, JustifyContentEnd)) {
            actions?.forEach {
                dashboardAction(it)
            }
        }
        popoverActions?.let {
            buttonPopover(SvgFile.Dots.filled, Height(3)) {
                textBlock("hello actions")
            }
        }
    }
}

fun ViewScope.dashboardAction(action: MenuAction) {
    button(action.onClick) {
        textBlock(action.label, modify(TextUppercase, TextSmall, OpacityHigh, Bold, PaddingX1))
    }
}