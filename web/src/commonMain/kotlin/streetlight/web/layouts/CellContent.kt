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
    row(modify(MinHeight(4), MinWidth(16), FlexWrap, FlexItems1, Gap2Px, TextAlignCenter, MoonShadow, mod)) {
        block()
    }
}

fun FlowContent.cellGrid(
    cells: List<EntityCell>,
    buttons: List<EntityButton>,
    mod: Modifier? = null,
) {
    if (cells.isEmpty() && buttons.isEmpty()) return
    cellBlock(mod) {
        cells.forEach { entityCell(it) }
        if (buttons.isNotEmpty()) {
            buttonsCell(MinWidth(32)) {
                buttons.forEach { it.block(this) }
            }
        }
    }
}

fun FlowContent.cell(
    svg: Svg? = null,
    text: String? = null,
    minWidth: Modifier? = MinWidth(16),
    mod: Modifier? = null,
    block: DIV.() -> Unit = {}
) {
    box(minWidth) {
        row(modify(CellContent.CellMod, mod)) {
            cellContent(svg, text, block)
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
    block: DIV.() -> Unit = {}
) {
    svg?.let {
        icon(svg, CellContent.IconMod)
    }
    text?.let {
        textBlock(it, CellContent.TextMod)
    }
    block()
}

fun FlowContent.entityCell(cell: EntityCell) {
    fun FlowContent.cellRow() = row(CellContent.CellMod) {
        icon(cell.icon, CellContent.IconMod)
        textBlock(cell.text, CellContent.TextMod)
    }

    when (val url = cell.url) {
        null -> box(MinWidth(16)) { cellRow() }
        else -> navigation(url.value, modify(Box, MinWidth(16))) { cellRow() }
    }
}

fun startsAtCell(startsAt: Instant?) = startsAt?.let {
    EntityCell(SvgFile.Clock, it.toTimeFormat(), null)
}

fun dateCell(startsAt: Instant) = EntityCell(SvgFile.Calendar, startsAt.toFutureFormat(), null)

fun FlowContent.exampleStartsAtCell() {
//    cell {
//        textBlock("[Day]", modify(CellContent.TextMod, ColorSchemeFg))
//        textBlock("[Time]", modify(CellContent.TextMod, MarginLeft(1)))
//    }
}

fun costCell(cost: Float, purchaseUrl: Url?): EntityCell {
    val ticketsUrl = cost.takeIf { it != 0f }?.let {
        purchaseUrl
    }
    val costText = when (cost) {
        0f -> "FREE"
        else -> "$${cost.format(2, true)}"
    }
    return EntityCell(SvgFile.TicketSmall, costText, ticketsUrl)
}

fun starCell(username: Username?) = EntityCell(SvgFile.SomeoneSmall, username?.value ?: "Guest", null)

fun FlowContent.textPropertyCell(property: String, value: String) {
    cell {
        textBlock(value, modify(CellContent.TextMod, MarginLeft(1)))
    }
}

fun postedAtCell(postedAt: Instant) = EntityCell(SvgFile.Clock, postedAt.toAgoFormat(), null)

fun linkCell(link: ExtraLink) = EntityCell(SvgFile.Link, link.label, link.url)

fun FlowContent.moreButton() {
    cellButton(SvgFile.Info) {
        onClick = KoalaFun.ToggleAncestor.invokeJs(ThisElement, FeedRow.Base, FeedRow.ToggleExpand)
    }
}

data class EntityCell(
    val icon: Svg,
    val text: String,
    val url: Url?,
)

data class EntityButton(
    val block: DIV.() -> Unit
)