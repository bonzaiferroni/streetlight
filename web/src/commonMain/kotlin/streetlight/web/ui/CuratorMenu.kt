package streetlight.web.ui

import kabinet.utils.toMetricString
import koala.SvgFile
import koala.css.AlignItemsCenter
import koala.css.BorderRadius50P
import koala.css.BorderRadiusPill
import koala.css.Class
import koala.css.Flex1
import koala.css.Gap0
import koala.css.Gap2Px
import koala.css.Height2
import koala.css.Height3
import koala.css.InkBg
import koala.css.JustifyContentCenter
import koala.css.MoonShadow
import koala.css.OpacityHalf
import koala.css.OpacityHigh
import koala.css.OpacityLow
import koala.css.Outline
import koala.css.OverflowClip
import koala.css.PaddingX2
import koala.css.PaperBg
import koala.css.PointerEventsAuto
import koala.css.PointerEventsNone
import koala.css.VoidBg
import koala.css.Width10
import koala.css.ZIndex1
import koala.css.ZenBg
import koala.css.addModifiers
import koala.css.modify
import koala.html.Attribute
import koala.html.box
import koala.html.button
import koala.html.column
import koala.html.icon
import koala.html.jsonAttributeOf
import koala.html.setAttribute
import koala.html.setJsonData
import koala.html.setPopoverTarget
import koala.html.textBlock
import koala.html.uuidAttributeOf
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
    val MarkIndicatorId = uuidAttributeOf("mark-indicator-id") { MarkId(it) }
    val MarkTallyId = uuidAttributeOf("mark-tally-id") { MarkId(it) }
    val MarkButtonId = uuidAttributeOf("mark-button-id") { MarkId(it) }
    val MarkBarId = uuidAttributeOf("mark-bar-id") { MarkId(it) }

    fun postLeanTextOf(sum: Int?) = sum?.takeIf { it != 0 }?.toMetricString() ?: "•"
}

//language="CSS"
val CuratorMenuStyle get() = with(CuratorMenu) {"""
$MarkButtonId {
    transition: var(--transition-background-color);
    
    &${Attribute.IsOn.to(true)} { 
        background-color: var(--system-fg);
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
    box(modify(Width10, BorderRadius50P, Outline, MoonShadow, OverflowClip, PaperBg, OpacityHigh)) {
        column(modify(Gap0)) {
            button(modify(InkBg, Flex1, OpacityHalf)) {
                configureMarkButton(upMark)
                onClick = AppFun.UpdateMark.invokeJs(ThisElement)
            }
            // spacer(modify(Height2Px, InkBg, OpacityHalf))
            button(modify(InkBg, Flex1, OpacityLow)) {
                configureMarkButton(downMark)
                onClick = AppFun.UpdateMark.invokeJs(ThisElement)
            }
        }
        column(modify(Gap0, AlignItemsCenter, JustifyContentCenter, ZIndex1, PointerEventsNone)) {
            icon(SvgFile.ChevronUp, modify(Height3)) {
                configMarkIndicator(upMark)
            }
            button(modify(PaddingX2, VoidBg, BorderRadiusPill, PointerEventsAuto)) {
                setPopoverTarget(PopoverId.Curator)
                textBlock {
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