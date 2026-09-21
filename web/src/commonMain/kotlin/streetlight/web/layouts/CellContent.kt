package streetlight.web.layouts

import kabinet.utils.format
import kabinet.utils.toAgoFormat
import kabinet.utils.toFutureFormat
import kabinet.utils.toTimeFormat
import kampfire.api.Username
import kampfire.model.Url
import koala.Svg
import koala.SvgFile
import koala.modifier.*
import kotlinx.css.px
import koala.html.*
import koala.interop.KoalaFun
import koala.interop.ThisElement
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.onClick
import streetlight.model.data.ExtraLink
import kotlin.time.Instant

object CellContent {
    val Container = Class("cell-content")
    val CellMod = modify(AlignItemsCenter, CardBg, Gap(0), Padding(1))
    val DualCellMod = modify(Gap2Px, FlexItems1)
    val IconMod = modify(SmallIconHeight, MarginRight(4.px), ColorSchemeBg)
    val ButtonIconMod = modify(SmallIconHeight, OpacityHigh)
    val ThumbMod = modify(SmallIconHeight, Aspect1, BorderRadius2, MarginRight(4.px))
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
    mod: Modifier? = null,
    block: FlowContent.() -> Unit = {}
) {
    row(modify(mod, MinHeight(4), MinWidth(16), FlexWrap, FlexItems1, Gap2Px, TextAlignCenter, MoonShadow)) {
        block()
    }
}

fun FlowContent.cell(
    svg: Svg? = null,
    text: String? = null,
    label: String? = null,
    minWidth: Modifier? = MinWidth(16),
    mod: Modifier? = null,
    block: DIV.() -> Unit = {}
) {
    box(minWidth) {
        row(modify(CellContent.CellMod, mod)) {
            cellContent(svg, text, label, block)
        }
    }
}

fun FlowContent.buttonsCell(
    minWidth: Modifier? = MinWidth(16),
    mod: Modifier? = null,
    block: DIV.() -> Unit = {},
) {
    box(modify(mod, minWidth, CardBg)) {
        row(modify(AlignItemsCenter, JustifyContentSpaceAround, Padding(1), Gap(2)), block)
    }
}

fun FlowContent.cellButton(
    svg: Svg,
    mod: Modifier? = null,
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
        textBlock("$it:", modify(CellContent.TextMod, InkDimFg))
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
    mod: Modifier? = null,
    block: FlowContent.() -> Unit = {}
) {
    // cell(modifiers, block)
    when (url) {
        null -> cell(svg, text, mod = mod, block = block)
        else -> {
            navigation(url.value, modify(Box, MinWidth(12))) {
                row(modify(CellContent.CellMod, mod)) {
                    cellContent(svg, text, null, block)
                }
            }
        }
    }
}

fun FlowContent.startsAtCell(startsAt: Instant?) {
    val startsAt = startsAt ?: return
    cell(SvgFile.Clock, startsAt.toTimeFormat())
}

fun FlowContent.dateCell(startsAt: Instant) {
    cell(SvgFile.Calendar, startsAt.toFutureFormat())
}

fun FlowContent.exampleStartsAtCell() {
//    cell {
//        textBlock("[Day]", modify(CellContent.TextMod, ColorSchemeFg))
//        textBlock("[Time]", modify(CellContent.TextMod, MarginLeft(1)))
//    }
}

fun FlowContent.costCell(cost: Float, purchaseUrl: Url?) {
    val ticketsUrl = cost.takeIf { it != 0f }?.let {
        purchaseUrl
    }
    val costText = when (cost) {
        0f -> "FREE"
        else -> "$${cost.format(2, true)}"
    }
    linkCell(ticketsUrl, SvgFile.TicketSmall, costText)
}

fun FlowContent.starCell(username: Username?) = cell(SvgFile.SomeoneSmall, username?.value ?: "Guest")

fun FlowContent.textPropertyCell(property: String, value: String) {
    cell {
        textBlock(value, modify(CellContent.TextMod, MarginLeft(1)))
    }
}

fun FlowContent.postedAtCell(postedAt: Instant) {
    cell(SvgFile.Clock, postedAt.toAgoFormat())
}

fun FlowContent.linkCell(link: ExtraLink) {
    linkCell(link.url, SvgFile.Link, link.label)
}

fun FlowContent.moreButton() {
    cellButton(SvgFile.Info) {
        onClick = KoalaFun.ToggleAncestor.invokeJs(ThisElement, FeedRow.Base, FeedRow.ToggleExpand)
    }
}