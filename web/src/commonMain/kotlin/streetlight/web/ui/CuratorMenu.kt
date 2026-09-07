package streetlight.web.ui

import kabinet.utils.toMetricString
import koala.SvgFile
import koala.css.AlignItemsCenter
import koala.css.BorderRadius50P
import koala.css.BorderRadiusPill
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
import koala.css.modify
import koala.html.box
import koala.html.button
import koala.html.column
import koala.html.icon
import koala.html.jsonAttributeOf
import koala.html.setAttribute
import koala.html.setJsonData
import koala.html.setPopoverTarget
import koala.html.textBlock
import kotlinx.html.FlowContent
import streetlight.model.data.CuratorStatus
import streetlight.model.data.VoteType

object CuratorMenu {
    val Attribute = jsonAttributeOf<CuratorStatus>("curator")
}

fun FlowContent.curatorBadge(curator: CuratorStatus) {
    when (curator.voteType) {
        VoteType.Polar -> polarBadge(curator)
        VoteType.Multi -> multiBadge(curator)
        VoteType.Single -> singleBadge(curator)
    }
}

fun FlowContent.polarBadge(curator: CuratorStatus) {
    box(modify(Width10, BorderRadius50P, Outline, MoonShadow, OverflowClip, PaperBg, OpacityHigh)) {
        column(modify(Gap0)) {
            button(modify(InkBg, Flex1, OpacityHalf))
            // spacer(modify(Height2Px, InkBg, OpacityHalf))
            button(modify(InkBg, Flex1, OpacityLow))
        }
        column(modify(Gap0, AlignItemsCenter, JustifyContentCenter, ZIndex1, PointerEventsNone)) {
            icon(SvgFile.ChevronUp, modify(Height3))
            button(modify(PaddingX2, VoidBg, BorderRadiusPill, PointerEventsAuto)) {
                setJsonData(CuratorMenu.Attribute, curator)
                // td: fix this
                // setAttribute(CuratorMenu.Attribute.to(curator))
                setPopoverTarget(PopoverId.Curator)
                textBlock(curator.light.toMetricString())
            }
            icon(SvgFile.ChevronDown, modify(Height3))
        }
    }
}

fun FlowContent.multiBadge(curator: CuratorStatus) {
    button {
        setJsonData(CuratorMenu.Attribute, curator)
        setPopoverTarget(PopoverId.Curator)
        column(modify(Width10, BorderRadius50P, Outline, ZenBg, MoonShadow, AlignItemsCenter, JustifyContentCenter, Gap2Px, OverflowClip)) {
            icon(SvgFile.Flame, modify(Height2, OpacityLow))
            textBlock(curator.light.toMetricString(), modify(PaddingX2, VoidBg, BorderRadiusPill))
            icon(SvgFile.ArrowsSort, modify(Height2, OpacityLow))
        }
    }
}

fun FlowContent.singleBadge(curator: CuratorStatus) {
    button {
        column(modify(Width10, BorderRadius50P, Outline, ZenBg, MoonShadow, AlignItemsCenter, JustifyContentCenter, Gap2Px, OverflowClip)) {
            icon(SvgFile.Flame, modify(Height2, OpacityLow))
            textBlock(curator.light.toMetricString(), modify(PaddingX2, VoidBg, BorderRadiusPill))
            icon(SvgFile.ChevronUp, modify(Height2, OpacityLow))
        }
    }
}