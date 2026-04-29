package streetlight.web.layouts

import kabinet.utils.format
import kabinet.utils.toRelativeDayFormat
import kabinet.utils.toTimeFormat
import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.EventId
import streetlight.model.data.GalaxyId
import streetlight.model.data.LocationId
import streetlight.web.ui.StarLightKey
import streetlight.web.ui.starLight
import kotlin.time.Instant

object CellContent {
    val RowMod = modify(JustifyContentCenter, AlignItemsCenter, WrapFlex, Gap0, PaddingY1, PaddingX2)
    val CardMod = modify(AlignItemsCenter, Gap0, BorderRadius0, JustifyContentCenter, MinWidth16, Padding0)
    val IconMod = modify(Height3, Aspect1, MarginRight1, ColorSchemeBg)
    val TextMod = modify()
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
        textBlock(startsAt.toRelativeDayFormat(), modify(CellContent.TextMod, Bold, ColorSchemeFg))
        textBlock(startsAt.toTimeFormat(), modify(CellContent.TextMod, MarginLeft1))
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

fun FlowContent.postedByCell(username: String?) {
    row(CellContent.RowMod) {
        icon(SvgFile.SomeoneSmall, CellContent.IconMod)
        textBlock(username ?: "Someone", CellContent.TextMod)
    }
}

fun FlowContent.propertyCell(property: String, value: String) {
    row(CellContent.RowMod) {
        textBlock("$property:", modify(CellContent.TextMod, Dim))
        textBlock(value, modify(CellContent.TextMod, MarginLeft1))
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

fun FlowContent.locationLightCell(locationId: LocationId) {
    starLight(null) {
        setData(StarLightKey.LocationLightId, locationId)
    }
}