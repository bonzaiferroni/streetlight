package streetlight.web.ui

import kabinet.utils.toMetricString
import koala.SvgFile
import koala.modifier.AlignItemsCenter
import koala.modifier.BorderRadius50P
import koala.modifier.BorderRadiusPill
import koala.modifier.CardBg
import koala.modifier.Class
import koala.modifier.Flex1
import koala.modifier.FlexColumn
import koala.modifier.Gap0
import koala.modifier.Gap2Px
import koala.modifier.Height2
import koala.modifier.Height3
import koala.modifier.HoverBg
import koala.modifier.JustifyContentCenter
import koala.modifier.MinWidth6
import koala.modifier.MoonShadow
import koala.modifier.OpacityHigh
import koala.modifier.OpacityLow
import koala.modifier.Outline
import koala.modifier.OverflowClip
import koala.modifier.PaddingX2
import koala.modifier.PointerEventsAuto
import koala.modifier.PointerEventsNone
import koala.modifier.TextAlignCenter
import koala.modifier.TextSmall
import koala.modifier.VoidBg
import koala.modifier.Width10
import koala.modifier.ZIndex1
import koala.modifier.ZenBg
import koala.modifier.addModifiers
import koala.modifier.modify
import koala.modifier.Attribute
import koala.html.box
import koala.html.button
import koala.html.column
import koala.html.icon
import koala.modifier.jsonAttributeOf
import koala.modifier.setAttribute
import koala.html.setJsonData
import koala.modifier.setPopoverTarget
import koala.html.textBlock
import koala.modifier.idAttributeOf
import koala.interop.ThisElement
import kotlinx.html.BUTTON
import kotlinx.html.CoreAttributeGroupFacade
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.onClick
import streetlight.model.data.CuratorStatus
import streetlight.model.data.FeedMark
import streetlight.model.data.MarkId
import streetlight.model.data.CuratorType
import streetlight.web.interop.AppFun

object CuratorMenu {
    val PostLean = Class("post-lean")
    val CuratorJson = jsonAttributeOf<CuratorStatus>("curator")
    val MarkIndicatorId = idAttributeOf("mark-indicator-id") { MarkId(it) }
    val MarkTallyId = idAttributeOf("mark-tally-id") { MarkId(it) }
    val MarkButtonId = idAttributeOf("mark-button-id") { MarkId(it) }
    val MarkBarId = idAttributeOf("mark-bar-id") { MarkId(it) }

    fun postLeanTextOf(sum: Int?) = sum?.takeIf { it != 0 }?.toMetricString() ?: "•"
}

//language="CSS"
val CuratorMenuStyle get() = with(CuratorMenu) {"""
$MarkButtonId {
    transition: var(--transition-background-color);
    
    &${Attribute.IsOn.to(true)} { 
        background-color: var(--system-bg) !important;
    }
}
"""}

fun FlowContent.curatorBadge(curator: CuratorStatus) {
    when (curator.curatorType) {
        CuratorType.Polar -> polarBadge(curator)
        CuratorType.Multi -> multiBadge(curator)
        CuratorType.Single -> singleBadge(curator)
    }
}

fun FlowContent.polarBadge(curator: CuratorStatus) {
    val upMark = curator.marks.first { it.lean.value > 0 }
    val downMark = curator.marks.first { it.lean.value < 0 }
    box(modify(Width10, BorderRadius50P, Outline, MoonShadow, OverflowClip, CardBg, OpacityHigh)) {
        column(modify(Gap0)) {
            button(modify(Flex1, HoverBg)) {
                configureMarkButton(upMark)
                onClick = AppFun.UpdateMark.invokeJs(ThisElement)
            }
            // spacer(modify(Height2Px, InkBg, OpacityHalf))
            button(modify(Flex1, HoverBg)) {
                configureMarkButton(downMark)
                onClick = AppFun.UpdateMark.invokeJs(ThisElement)
            }
        }
        column(modify(Gap0, AlignItemsCenter, JustifyContentCenter, ZIndex1, PointerEventsNone)) {
            icon(SvgFile.ChevronUp, modify(Height3)) {
                configMarkIndicator(upMark)
            }
            button(modify(Height3, MinWidth6, FlexColumn, JustifyContentCenter, VoidBg, BorderRadiusPill, PointerEventsAuto, Outline)) {
                setPopoverTarget(PopoverId.Curator)
                textBlock(mod = modify(TextAlignCenter, TextSmall)) {
                    configurePostLeanText(curator)
                }
            }
            icon(SvgFile.ChevronDown, modify(Height3)) {
                configMarkIndicator(downMark)
            }
        }
    }
}

fun BUTTON.configureMarkButton(mark: FeedMark) {
    setAttribute(CuratorMenu.MarkButtonId.to(mark.markId))
    setAttribute(Attribute.IsOn.to(mark.isMarked))
}

// td: may not need this
fun CoreAttributeGroupFacade.configMarkIndicator(mark: FeedMark) {
    setAttribute(CuratorMenu.MarkIndicatorId.to(mark.markId))
    setAttribute(Attribute.IsOn.to(mark.isMarked))
}

fun CoreAttributeGroupFacade.configureMarkTallyText(mark: FeedMark) {
    setAttribute(CuratorMenu.MarkTallyId.to(mark.markId))
    +mark.count.toMetricString()
}

fun CoreAttributeGroupFacade.configurePostLeanText(curator: CuratorStatus) {
    addModifiers(CuratorMenu.PostLean)
    +CuratorMenu.postLeanTextOf(curator.postLean)
}

fun DIV.configureMarkBar(mark: FeedMark) {
    setAttribute(CuratorMenu.MarkBarId.to(mark.markId))
}

fun FlowContent.multiBadge(curator: CuratorStatus) {
    button {
        setJsonData(CuratorMenu.CuratorJson, curator)
        setPopoverTarget(PopoverId.Curator)
        column(modify(Width10, BorderRadius50P, Outline, ZenBg, MoonShadow, AlignItemsCenter, JustifyContentCenter, Gap2Px, OverflowClip)) {
            icon(SvgFile.Flame, modify(Height2, OpacityLow))
            textBlock(curator.postLean.toMetricString(), modify(PaddingX2, VoidBg, BorderRadiusPill))
            icon(SvgFile.ArrowsSort, modify(Height2, OpacityLow))
        }
    }
}

fun FlowContent.singleBadge(curator: CuratorStatus) {
    button {
        column(modify(Width10, BorderRadius50P, Outline, ZenBg, MoonShadow, AlignItemsCenter, JustifyContentCenter, Gap2Px, OverflowClip)) {
            icon(SvgFile.Flame, modify(Height2, OpacityLow))
            textBlock(curator.postLean.toMetricString(), modify(PaddingX2, VoidBg, BorderRadiusPill))
            icon(SvgFile.ChevronUp, modify(Height2, OpacityLow))
        }
    }
}