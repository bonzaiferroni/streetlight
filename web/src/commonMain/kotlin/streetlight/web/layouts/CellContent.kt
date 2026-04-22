package streetlight.web.layouts

import kabinet.utils.format
import kabinet.utils.toRelativeDayFormat
import kabinet.utils.toTimeFormat
import koala.css.AlignItemsCenter
import koala.css.Bold
import koala.css.BorderRadius0
import koala.css.Dim
import koala.css.Gap0
import koala.css.JustifyContentCenter
import koala.css.MarginLeft1
import koala.css.MinWidth16
import koala.css.ModifierSet
import koala.css.Padding0
import koala.css.Padding1
import koala.css.WrapFlex
import koala.css.modify
import koala.html.navigationIfNotNull
import koala.html.card
import koala.html.row
import koala.html.setData
import koala.html.textBlock
import kotlinx.html.FlowContent
import streetlight.model.data.EventId
import streetlight.model.data.GalaxyId
import streetlight.model.data.LocationId
import streetlight.web.ui.StarLightKey
import streetlight.web.ui.starLight
import kotlin.time.Instant

object CellContent {
    val RowMod = modify(JustifyContentCenter, AlignItemsCenter, WrapFlex, Gap0, Padding1)
    val CardMod = modify(AlignItemsCenter, Gap0, BorderRadius0, JustifyContentCenter, MinWidth16, Padding0)
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
        textBlock(startsAt.toRelativeDayFormat(), modify(Bold))
        textBlock(startsAt.toTimeFormat(), modify(MarginLeft1))
    }
}

fun FlowContent.costCell(cost: Float?, purchaseUrl: String?) {
    val ticketsUrl = cost.takeIf { it != 0f }?.let {
        purchaseUrl
    }
    if (cost == 0f) {
        textBlock("FREE event")
    } else {
        val costText = when (cost) {
            null -> "check source"
            else -> "$${cost.format(2, true)}"
        }
        navigationIfNotNull(ticketsUrl) {
            row(CellContent.RowMod) {
                textBlock("tickets:", modify(Dim))
                textBlock(costText, modify(MarginLeft1))
            }
        }
    }
}

fun FlowContent.postedByCell(username: String?) {
    propertyCell("posted by", username ?: "someone")
}

fun FlowContent.propertyCell(property: String, value: String) {
    row(CellContent.RowMod) {
        textBlock("$property:", modify(Dim))
        textBlock(value, modify(MarginLeft1))
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