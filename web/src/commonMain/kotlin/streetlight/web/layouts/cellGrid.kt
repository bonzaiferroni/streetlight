package streetlight.web.layouts

import kabinet.utils.format
import kabinet.utils.toMetricString
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
import kotlinx.html.span
import streetlight.model.data.ExtraLink
import kotlin.time.Instant

/**
 * The facts of an entity as a grid of icon cells, with a row of [buttons] beneath. Nothing renders when both are
 * empty.
 *
 * A cell with a url is a link.
 */
fun FlowContent.cellGrid(
    cells: List<EntityCell>?,
    buttons: List<EntityButton>?,
    mod: Modifier? = null,
) {
    if (cells.isNullOrEmpty() && buttons.isNullOrEmpty()) return
    div(modify(CellGrid.Base, MinHeight(4), MinWidth(16), TextAlignCenter, MoonShadow, mod)) {
        cells?.forEach { cell ->
            when (val url = cell.url) {
                null -> row(CellGrid.CellMod) { cellContent(cell) }
                else -> navigation(url.value, modify(FlexRow, CellGrid.CellMod)) { cellContent(cell) }
            }
        }
        if (!buttons.isNullOrEmpty()) {
            row(modify(CardBg, AlignItemsCenter, JustifyContentSpaceAround, Padding(1), Gap(2))) {
                buttons.forEach { it.block(this) }
            }
        }
    }
}

private fun FlowContent.cellContent(cell: EntityCell) {
    icon(cell.icon, CellGrid.IconMod)
    textBlock(mod = CellGrid.TextMod) {
        +cell.text
        cell.label?.let { label ->
            span {
                addModifiers(CellGrid.LabelMod)
                +" $label"
            }
        }
    }
}

object CellGrid {
    val Base = Class("cell-grid")
    val CellMod = modify(AlignItemsCenter, CardBg, Gap(0), Padding(1))
    val IconMod = modify(SmallIconHeight, MarginRight(4.px), ColorSchemeBg)
    val ButtonIconMod = modify(SmallIconHeight, OpacityHigh)
    val TextMod = modify(TextSmall, SingleLine, TextOverflowEllipses, Flex1)
    val LabelMod = modify(OpacityHigh)
}

//language="CSS"
val CellGridCss get() = with(CellGrid) { """
$Base {
    display: flex;
    flex-wrap: wrap;
    gap: 2px;

    > * {
        flex: 1;
        min-width: var(--unit-16);
    }
}
""" }

/** The time of day [startsAt]. */
fun startsAtCell(startsAt: Instant) = EntityCell(SvgFile.Clock, startsAt.toTimeFormat(), null)

fun dateCell(startsAt: Instant) = EntityCell(SvgFile.Calendar, startsAt.toFutureFormat(), null)

fun FlowContent.exampleStartsAtCell() {
//    cell {
//        textBlock("[Day]", modify(CellContent.TextMod, ColorSchemeFg))
//        textBlock("[Time]", modify(CellContent.TextMod, MarginLeft(1)))
//    }
}

/** The cost, or FREE, linking to [purchaseUrl] when there is a cost. */
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

fun postedAtCell(postedAt: Instant) = EntityCell(SvgFile.Clock, postedAt.toAgoFormat(), null)

fun linkCell(link: ExtraLink) = EntityCell(SvgFile.Link, link.label, link.url)

fun locationCountCell(count: Int) = EntityCell(SvgFile.MapPin, count.toMetricString(), null, "locations")

fun eventCountCell(count: Int) = EntityCell(SvgFile.Calendar, count.toMetricString(), null, "events")

/** Expands and collapses the body of the feed row around it. */
fun FlowContent.moreButton() {
    icon(SvgFile.Info, modify(CellGrid.ButtonIconMod, FeedRow.MoreButton)) {
        onClick = KoalaFun.ToggleAncestor.invokeJs(ThisElement, FeedRow.Base, FeedRow.ToggleExpand)
    }
}

/** One cell of a [cellGrid]: an icon, a text with an optional dimmer [label], and a link when [url] is given. */
data class EntityCell(
    val icon: Svg,
    val text: String,
    val url: Url?,
    val label: String? = null,
)

data class EntityButton(
    val block: DIV.() -> Unit
)