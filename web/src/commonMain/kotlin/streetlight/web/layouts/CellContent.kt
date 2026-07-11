package streetlight.web.layouts

import kabinet.utils.format
import kabinet.utils.toAgoFormat
import kabinet.utils.toRelativeDayFormat
import kabinet.utils.toTimeFormat
import kampfire.api.Username
import kampfire.model.Url
import kampfire.model.toUrl
import koala.Svg
import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.onClick
import streetlight.model.data.ExtraLink
import kotlin.time.Instant

object CellContent {
    val Container = Class("cell-content")
    val CellMod = modify(AlignItemsCenter, CardBg, Gap0, Padding1)
    val DualCellMod = modify(GapTiny, FlexItems1)
    val IconMod = modify(Height3, MarginRight4Px, ColorSchemeBg)
    val ButtonIconMod = modify(Height3, OpacityHigh)
    val ThumbMod = modify(Height3, Aspect1, BorderRadius2, MarginRight4Px)
    val TextMod = modify(TextSmall, SingleLine, TextOverflowEllipses, Flex1)
}

//language="CSS"
val CellContentCss get() = with(CellContent) { """
$Container {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(12rem, 1fr));
    gap: 2px;
}
""" }

fun FlowContent.cellBlock(
    mod: ModifierSet? = null,
    block: FlowContent.() -> Unit = {}
) {
    row(modify(mod, MinHeight4, MinWidth16, FlexWrap, FlexItems1, GapTiny, TextAlignCenter, MoonShadow)) {
        block()
    }
}

fun FlowContent.cell(
    svg: Svg? = null,
    text: String? = null,
    label: String? = null,
    minWidth: Modifier? = MinWidth16,
    mod: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    box(modify(minWidth)) {
        row(modify(CellContent.CellMod, mod)) {
            cellContent(svg, text, label, block)
        }
    }
}

fun FlowContent.buttonsCell(
    minWidth: Modifier? = MinWidth16,
    mod: ModifierSet? = null,
    block: DIV.() -> Unit = {},
) {
    box(modify(mod, minWidth, CardBg)) {
        row(modify(AlignItemsCenter, JustifyContentSpaceAround, Padding1, Gap2), block)
    }
}

fun FlowContent.cellButton(
    svg: Svg,
    mod: ModifierSet? = null,
    block: DIV.() -> Unit = {},
) {
    icon(svg, modify(CellContent.ButtonIconMod, mod)) {
        block()
    }
}

fun DIV.cellContent(
    svg: Svg? = null,
    text: String? = null,
    label: String? = null,
    block: DIV.() -> Unit = {}
) {
    label?.let {
        textBlock("$it:", modify(CellContent.TextMod, Dim))
    }
    svg?.let {
        icon(svg, CellContent.IconMod)
    }
    text?.let {
        textBlock(it, CellContent.TextMod)
    }
    block()
}

fun FlowContent.linkCell(
    url: Url?,
    svg: Svg,
    text: String?,
    mod: ModifierSet? = null,
    block: FlowContent.() -> Unit = {}
) {
    // cell(modifiers, block)
    when (url) {
        null -> cell(svg, text, mod = mod, block = block)
        else -> {
            navigation(url.value, modify(Box, MinWidth12)) {
                row(modify(CellContent.CellMod, mod)) {
                    cellContent(svg, text, null, block)
                }
            }
        }
    }
}

fun FlowContent.startsAtCell(startsAt: Instant) {
    cell(SvgFile.Clock, startsAt.toTimeFormat())
}

fun FlowContent.dateCell(startsAt: Instant) {
    cell(SvgFile.Calendar, startsAt.toRelativeDayFormat())
}

fun FlowContent.exampleStartsAtCell() {
//    cell {
//        textBlock("[Day]", modify(CellContent.TextMod, ColorSchemeFg))
//        textBlock("[Time]", modify(CellContent.TextMod, MarginLeft1))
//    }
}

fun FlowContent.costCell(cost: Float?, purchaseUrl: Url?) {
    val ticketsUrl = cost.takeIf { it != 0f }?.let {
        purchaseUrl
    }
    val costText = when (cost) {
        0f -> "FREE"
        null -> "check source"
        else -> "$${cost.format(2, true)}"
    }
    linkCell(ticketsUrl, SvgFile.TicketSmall, costText)
}

fun FlowContent.starCell(username: Username?) = cell(SvgFile.SomeoneSmall, username?.value ?: "Guest")

fun FlowContent.textPropertyCell(property: String, value: String) {
    cell {
        textBlock(value, modify(CellContent.TextMod, MarginLeft1))
    }
}

fun FlowContent.postedAtCell(postedAt: Instant) {
    cell(SvgFile.Clock, postedAt.toAgoFormat())
}

fun FlowContent.linkCell(link: ExtraLink) {
    linkCell(link.url.toUrl(), SvgFile.Link, link.label)
}

fun FlowContent.moreButton() {
    cellButton(SvgFile.Info) {
        onClick = KoalaFun.ToggleAncestor.invoke(ThisElement, FeedRow.Base, FeedPostLegacy.ToggleExpand)
    }
}