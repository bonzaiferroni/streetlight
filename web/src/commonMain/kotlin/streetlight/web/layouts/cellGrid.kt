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

object CellGrid {
    val CellMod = modify(AlignItemsCenter, CardBg, Gap(0), Padding(1))
    val IconMod = modify(SmallIconHeight, MarginRight(4.px), ColorSchemeBg)
    val ButtonIconMod = modify(SmallIconHeight, OpacityHigh)
    val TextMod = modify(TextSmall, SingleLine, TextOverflowEllipses, Flex1)
}

fun FlowContent.cellGrid(
    cells: List<EntityCell>,
    buttons: List<EntityButton>,
    mod: Modifier? = null,
) {
    if (cells.isEmpty() && buttons.isEmpty()) return
    row(modify(MinHeight(4), MinWidth(16), FlexWrap, FlexItems1, Gap2Px, TextAlignCenter, MoonShadow, mod)) {
        cells.forEach { cell ->
            fun FlowContent.cellRow() = row(CellGrid.CellMod) {
                icon(cell.icon, CellGrid.IconMod)
                textBlock(cell.text, CellGrid.TextMod)
            }

            when (val url = cell.url) {
                null -> box(MinWidth(16)) { cellRow() }
                else -> navigation(url.value, modify(Box, MinWidth(16))) { cellRow() }
            }
        }
        if (buttons.isNotEmpty()) {
            box(modify(MinWidth(16), CardBg)) {
                row(modify(AlignItemsCenter, JustifyContentSpaceAround, Padding(1), Gap(2))) {
                    buttons.forEach { it.block(this) }
                }
            }
        }
    }
}

fun FlowContent.cell(
    svg: Svg? = null,
    text: String? = null,
    block: DIV.() -> Unit = {}
) {
    box(MinWidth(16)) {
        row(CellGrid.CellMod) {
            svg?.let { icon(it, CellGrid.IconMod) }
            text?.let { textBlock(it, CellGrid.TextMod) }
            block()
        }
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
        textBlock(value, modify(CellGrid.TextMod, MarginLeft(1)))
    }
}

fun postedAtCell(postedAt: Instant) = EntityCell(SvgFile.Clock, postedAt.toAgoFormat(), null)

fun linkCell(link: ExtraLink) = EntityCell(SvgFile.Link, link.label, link.url)

fun FlowContent.moreButton() {
    icon(SvgFile.Info, CellGrid.ButtonIconMod) {
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