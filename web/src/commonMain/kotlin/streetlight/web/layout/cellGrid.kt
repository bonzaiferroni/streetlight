package streetlight.web.layout

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
import kotlinx.html.DIV
import kotlinx.html.P
import kotlinx.html.FlowContent
import kotlinx.html.span
import koala.model.ExtraLink
import kotlin.time.Instant
import koala.component.*
import streetlight.model.data.EntityType
import streetlight.web.model.cssValue
import streetlight.model.data.EntityDetail

/**
 * The facts of an entity as a grid of icon cells, followed by a cell of [buttons] that fills the rest of its row.
 * Nothing renders when both are empty.
 *
 * A cell with a route or a url is a link.
 */
fun FlowContent.cellGrid(
    cells: List<EntityDetail>?,
    buttons: List<EntityButton>?,
    mod: Modifier? = null,
) {
    if (cells.isNullOrEmpty() && buttons.isNullOrEmpty()) return
    div(modify(CellGrid.Base, MinHeight(4), MinWidth(16), TextAlignCenter, MoonShadow, SchemeBg, mod)) {
        cells?.forEach { cell ->
            when (val path = cell.route?.toRelativePath() ?: cell.url?.value) {
                null -> row(CellGrid.CellMod) {
                    cell.type?.let {
                        setStyle(Css.ColorScheme.of(it.cssValue))
                    }
                    cellContent(cell)
                }
                else -> navigation(path, modify(FlexRow, CellGrid.CellMod)) {
                    cell.type?.let {
                        setStyle(Css.ColorScheme.of(it.cssValue))
                    }
                    cellContent(cell)
                }
            }
        }
//        if (!buttons.isNullOrEmpty()) {
//            row(modify(CellGrid.Buttons, CardBg, JustifyContentEnd, PrimaryLightFg)) {
//                row(modify(AlignItemsCenter, JustifyContentSpaceAround, Padding(1), Gap(2))) {
//                    buttons.forEach { it.block(this) }
//                }
//            }
//        }
    }
}

/** The detail's icon, or else its label, in the scheme color, then its text. */
private fun FlowContent.cellContent(cell: EntityDetail) {
    row(CellGrid.ContentMod) {
        val svg = cell.svg
        val label = cell.label
        when {
            svg != null -> icon(svg, CellGrid.IconMod)
            label != null -> span {
                addModifiers(CellGrid.LabelMod)
                +label
            }
        }
        textBlock(cell.text, CellGrid.TextMod)
    }
}

/**
 * A cell that fills out the last, wider cell of a grid with quiet extra content, such as spare tags, an owner or a
 * date, after [icon]. Its text stays on one line and ends in an ellipsis, so it never makes the grid longer.
 */
fun FlowContent.fillerCell(icon: Svg, content: P.() -> Unit) {
    row(CellGrid.CellMod) {
        row(CellGrid.ContentMod) {
            icon(icon, CellGrid.IconMod)
            textBlock(mod = modify(CellGrid.TextMod, OpacityHigh), block = content)
        }
    }
}

object CellGrid {
    val Base = Class("cell-grid")
    val Buttons = Base.withBemElement("buttons")
    val CellMod = modify(CardBg)
    val ContentMod = modify(AlignItemsCenter, Gap(0), Padding(1))
    val IconMod = modify(SmallIconHeight, MarginRight(4.px), ColorSchemeBg)
    val ButtonIconMod = modify(SmallIconHeight, OpacityHigh)
    val TextMod = modify(TextSmall, SingleLine, TextOverflowEllipses, Flex1, TextAlignCenter)
    val LabelMod = modify(TextSmall, MarginRight(4.px), ColorSchemeFg)
}

//language="CSS"
val CellGridCss get() = with(CellGrid) { """
$Base {
    container-type: inline-size;
    display: flex;
    flex-wrap: wrap;
    gap: 2px;

    > * {
        --cols: 1;
        --cell-width: calc((100cqi - (var(--cols) - 1) * 2px) / var(--cols));
        width: var(--cell-width);
        height: calc(var(--unit) * 5);
        min-width: 0;

        @container (width >= 258px) { --cols: 2; }
        @container (width >= 388px) { --cols: 3; }
        @container (width >= 518px) { --cols: 4; }
        @container (width >= 648px) { --cols: 5; }
        @container (width >= 778px) { --cols: 6; }
        @container (width >= 908px) { --cols: 7; }
    }

    > :last-child {
        flex-grow: 1;
    }

    > * > * {
        flex: 1;
        min-width: 0;
    }
}
""" }

/** The time of day [startsAt]. */
fun startsAtCell(startsAt: Instant) = EntityDetail(startsAt.toTimeFormat(), SvgFile.Clock.small)

fun starCell(username: Username?) = EntityDetail(username?.value ?: "Guest", SvgFile.SomeoneSmall)

fun postedAtCell(postedAt: Instant) = EntityDetail(postedAt.toAgoFormat(), SvgFile.Clock.small)

fun linkCell(link: ExtraLink) = EntityDetail(link.label, SvgFile.Link.small, url = link.url)

data class EntityButton(
    val block: DIV.() -> Unit
)