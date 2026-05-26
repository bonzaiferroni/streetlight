package streetlight.web.layouts

import kabinet.utils.format
import kabinet.utils.toAgoFormat
import kabinet.utils.toRelativeDayFormat
import kabinet.utils.toTimeFormat
import kampfire.model.Url
import koala.Svg
import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import streetlight.model.data.EventEdit
import streetlight.model.data.EventId
import streetlight.model.data.EventLocation
import streetlight.model.data.Galaxy
import streetlight.model.data.GalaxyId
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.LocationId
import streetlight.web.ui.StarLightKey
import streetlight.web.ui.starLight
import kotlin.time.Instant

object CellContent {
    val RowMod = modify(JustifyContentCenter, AlignItemsCenter, FlexWrap, Gap0, PaddingY1, PaddingX2)
    val CardMod = modify(AlignItemsCenter, Gap0, BorderRadius0, JustifyContentCenter, MinWidth16, Padding0)
    val IconMod = modify(Height3, Aspect1, MarginRight1, ColorSchemeBg)
    val ThumbMod = modify(Height3, Aspect1, BorderRadius2, MarginRight1)
    val TextMod = modify()
}

fun FlowContent.cellRow(
    cells: List<(FlowContent.() -> Unit)?>,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    row(modify(modifiers, MinHeight6, MinWidth24, FlexItems1, GapTiny, TextAlignCenter, MoonShadow)) {
        block()
        cells.forEach {
            val cell = it ?: return@forEach
            cellCard {
                cell()
            }
        }
    }
}

fun FlowContent.cellCard(
    modifiers: ModifierSet? = null,
    block: FlowContent.() -> Unit = {}
) {
    card(modify(CellContent.CardMod, modifiers)) {
        block()
    }
}

fun FlowContent.startsAtCell(startsAt: Instant) {
    row(CellContent.RowMod) {
        textBlock(startsAt.toRelativeDayFormat(), modify(CellContent.TextMod, ColorSchemeFg))
        textBlock(startsAt.toTimeFormat(), modify(CellContent.TextMod, MarginLeft1))
    }
}

fun FlowContent.exampleStartsAtCell() {
    row(CellContent.RowMod) {
        textBlock("[Day]", modify(CellContent.TextMod, ColorSchemeFg))
        textBlock("[Time]", modify(CellContent.TextMod, MarginLeft1))
    }
}

fun FlowContent.costCell(cost: Float?, purchaseUrl: String?) {
    val ticketsUrl = cost.takeIf { it != 0f }?.let {
        purchaseUrl
    }
    val costText = when (cost) {
        0f -> "FREE"
        null -> "check source"
        else -> "$${cost.format(2, true)}"
    }
    navigationIfNotNull(ticketsUrl) {
        row(CellContent.RowMod) {
            icon(SvgFile.TicketSmall, CellContent.IconMod)
            textBlock(costText, modify(CellContent.TextMod))
        }
    }
}

fun FlowContent.starCell(username: String?) = iconPropertyCell(SvgFile.SomeoneSmall, username ?: "Someone")

fun FlowContent.starCell(username: String?, userThumb: Url?) {
    row(CellContent.RowMod) {
        image(userThumb, CellContent.ThumbMod)
        textBlock(username ?: "Someone", CellContent.TextMod)
    }
}

fun FlowContent.textPropertyCell(property: String, value: String) {
    row(CellContent.RowMod) {
        textBlock("$property:", modify(CellContent.TextMod, Dim))
        textBlock(value, modify(CellContent.TextMod, MarginLeft1))
    }
}

fun FlowContent.postedAtCell(postedAt: Instant) {
    iconPropertyCell(SvgFile.Clock, postedAt.toAgoFormat())
}

fun FlowContent.iconPropertyCell(icon: Svg, value: String) {
    row(CellContent.RowMod) {
        icon(icon, CellContent.IconMod)
        textBlock(value, CellContent.TextMod)
    }
}

fun FlowContent.galaxyLightCell(visibility: Int?, galaxyId: GalaxyId) {
    starLight(visibility, CellContent.RowMod) {
        setData(StarLightKey.GalaxyLightId, galaxyId)
    }
}

fun FlowContent.eventLightCell(visibility: Int?, eventId: EventId) {
    starLight(visibility, CellContent.RowMod) {
        setData(StarLightKey.EventLightId, eventId)
    }
}

fun FlowContent.locationLightCell(visibility: Int?, locationId: LocationId) {
    starLight(visibility) {
        setData(StarLightKey.LocationLightId, locationId)
    }
}

fun FlowContent.exampleLightCell() {
    starLight(0)
}

fun locationCells(location: Location): List<(FlowContent.() -> Unit)?> = listOf(
    { starCell(location.username) },
    { locationLightCell(location.lightCount, location.locationId)}
)

fun locationCells(username: String?, edit: LocationEdit): List<(FlowContent.() -> Unit)?> = listOf(
    { starCell(username) },
    { exampleLightCell() }
)

fun eventCells(event: EventEdit): List<(FlowContent.() -> Unit)?> = listOf(
    { when (val startsAt = event.startsAt) {
        null -> exampleStartsAtCell()
        else -> startsAtCell(startsAt)
    } },
    { costCell(event.cost, event.url) },
    { exampleLightCell() },
)

fun eventCells(event: EventLocation): List<(FlowContent.() -> Unit)?> = listOf(
    { startsAtCell(event.startsAt) },
    { costCell(event.cost, event.url) },
    { eventLightCell(event.lightCount, event.eventId) },
)

fun galaxyCells(galaxy: Galaxy): List<(FlowContent.() -> Unit)?> = listOf(
    { iconPropertyCell(SvgFile.Calendar, galaxy.eventCount?.toString() ?: "?") },
    { galaxyLightCell(galaxy.lightCount, galaxy.galaxyId) }
)