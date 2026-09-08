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
import koala.html.Attribute
import koala.html.booleanAttributeOf
import koala.html.box
import koala.html.button
import koala.html.column
import koala.html.enumAttributeOf
import koala.html.icon
import koala.html.intAttributeOf
import koala.html.jsonAttributeOf
import koala.html.setAttribute
import koala.html.setJsonData
import koala.html.setPopoverTarget
import koala.html.textBlock
import koala.html.uuidAttributeOf
import koala.interop.ThisElement
import kotlinx.html.BUTTON
import kotlinx.html.CoreAttributeGroupFacade
import kotlinx.html.FlowContent
import kotlinx.html.onClick
import streetlight.model.data.CuratorStatus
import streetlight.model.data.Mark
import streetlight.model.data.Lean
import streetlight.model.data.MarkId
import streetlight.model.data.MarkStatus
import streetlight.model.data.PostId
import streetlight.model.data.VoteType
import streetlight.web.interop.AppFun

object CuratorMenu {
    val CuratorJson = jsonAttributeOf<CuratorStatus>("curator")
    val IsMarked = booleanAttributeOf("is-marked")
    val MarkTally = intAttributeOf("mark-tally")
    val PostLean = intAttributeOf("post-lean")
    val PostLeanId = uuidAttributeOf("post-lean-id") { PostId(it) }
    val Lean = enumAttributeOf<Lean>("mark-lean")
    val MarkIndicatorId = uuidAttributeOf("mark-indicator-id") { MarkId(it) }
    val MarkTallyId = uuidAttributeOf("mark-tally-id") { MarkId(it) }
    val MarkButtonId = uuidAttributeOf("mark-button-id") { MarkId(it) }
    val UnmarkId = uuidAttributeOf("unmark-id") { MarkId(it) }

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
    when (curator.voteType) {
        VoteType.Polar -> polarBadge(curator)
        VoteType.Multi -> multiBadge(curator)
        VoteType.Single -> singleBadge(curator)
    }
}

fun FlowContent.polarBadge(curator: CuratorStatus) {
    val postMarks = curator.postMarks
    val upMark = curator.marks.first { it.lean.value > 0 }
    val upStatus = postMarks?.firstOrNull { it.markId == upMark.markId }
    val downMark = curator.marks.first { it.lean.value < 0 }
    val downStatus = postMarks?.firstOrNull { it.markId == downMark.markId }
    box(modify(Width10, BorderRadius50P, Outline, MoonShadow, OverflowClip, PaperBg, OpacityHigh)) {
        column(modify(Gap0)) {
            button(modify(InkBg, Flex1, OpacityHalf)) {
                configMarkButton(curator.postId, upMark, upStatus, downMark)
            }
            // spacer(modify(Height2Px, InkBg, OpacityHalf))
            button(modify(InkBg, Flex1, OpacityLow)) {
                configMarkButton(curator.postId, downMark, downStatus, upMark)
            }
        }
        column(modify(Gap0, AlignItemsCenter, JustifyContentCenter, ZIndex1, PointerEventsNone)) {
            icon(SvgFile.ChevronUp, modify(Height3)) {
                configMarkIndicator(curator.postId, upMark, upStatus, downMark)
            }
            button(modify(PaddingX2, VoidBg, BorderRadiusPill, PointerEventsAuto)) {
                setJsonData(CuratorMenu.CuratorJson, curator)
                // td: fix this
                // setAttribute(CuratorMenu.Attribute.to(curator))
                setPopoverTarget(PopoverId.Curator)
                textBlock {
                    configurePostLeanText(curator.postId, curator.postTally)
                }
            }
            icon(SvgFile.ChevronDown, modify(Height3)) {
                configMarkIndicator(curator.postId, downMark, downStatus, upMark)
            }
        }
    }
}

fun BUTTON.configMarkButton(postId: PostId, mark: Mark, status: MarkStatus?, unmark: Mark?) {
    setAttribute(AppAttribute.PostId.to(postId))
    setAttribute(CuratorMenu.MarkButtonId.to(mark.markId))
    setAttribute(Attribute.IsOn.to(status?.isMarked ?: false))
    setAttribute(CuratorMenu.Lean.to(mark.lean))
    unmark?.let {
        setAttribute(CuratorMenu.UnmarkId.to(it.markId))
    }
    onClick = AppFun.UpdateMark.invokeJs(ThisElement)
}

// td: may not need this
fun CoreAttributeGroupFacade.configMarkIndicator(postId: PostId, mark: Mark, status: MarkStatus?, unmark: Mark?) {
    setAttribute(AppAttribute.PostId.to(postId))
    setAttribute(CuratorMenu.MarkIndicatorId.to(mark.markId))
    setAttribute(Attribute.IsOn.to(status?.isMarked ?: false))
    unmark?.let {
        setAttribute(CuratorMenu.UnmarkId.to(it.markId))
    }
}

fun CoreAttributeGroupFacade.configMarkTallyText(postId: PostId, mark: Mark, status: MarkStatus?) {
    val tally = status?.sum ?: 0
    setAttribute(AppAttribute.PostId.to(postId))
    setAttribute(CuratorMenu.MarkTallyId.to(mark.markId))
    setAttribute(CuratorMenu.MarkTally.to(tally))
    +tally.toMetricString()
}

fun CoreAttributeGroupFacade.configurePostLeanText(postId: PostId, lean: Int) {
    setAttribute(CuratorMenu.PostLeanId.to(postId))
    setAttribute(CuratorMenu.PostLean.to(lean))
    +CuratorMenu.postLeanTextOf(lean)
}

fun FlowContent.multiBadge(curator: CuratorStatus) {
    button {
        setJsonData(CuratorMenu.CuratorJson, curator)
        setPopoverTarget(PopoverId.Curator)
        column(modify(Width10, BorderRadius50P, Outline, ZenBg, MoonShadow, AlignItemsCenter, JustifyContentCenter, Gap2Px, OverflowClip)) {
            icon(SvgFile.Flame, modify(Height2, OpacityLow))
            textBlock(curator.postTally.toMetricString(), modify(PaddingX2, VoidBg, BorderRadiusPill))
            icon(SvgFile.ArrowsSort, modify(Height2, OpacityLow))
        }
    }
}

fun FlowContent.singleBadge(curator: CuratorStatus) {
    button {
        column(modify(Width10, BorderRadius50P, Outline, ZenBg, MoonShadow, AlignItemsCenter, JustifyContentCenter, Gap2Px, OverflowClip)) {
            icon(SvgFile.Flame, modify(Height2, OpacityLow))
            textBlock(curator.postTally.toMetricString(), modify(PaddingX2, VoidBg, BorderRadiusPill))
            icon(SvgFile.ChevronUp, modify(Height2, OpacityLow))
        }
    }
}