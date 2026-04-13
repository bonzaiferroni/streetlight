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
import koala.css.WidthAuto
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
import streetlight.web.ui.StarLightKey
import streetlight.web.ui.starLight
import kotlin.time.Instant

object CellContent {
    val RowMod = modify(JustifyContentCenter, WrapFlex, Gap0)
    val CardMod = modify(AlignItemsCenter, Gap0, BorderRadius0, JustifyContentCenter, MinWidth16)
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

fun FlowContent.postedBy(username: String?) {
    row(CellContent.RowMod) {
        textBlock("posted by:", modify(Dim))
        textBlock(username ?: "someone", modify(MarginLeft1))
    }
}

fun FlowContent.lightCell(galaxyId: GalaxyId) {
    row(modify(WidthAuto)) {
        setData(StarLightKey.GalaxyLightId, galaxyId)
        starLight((0..10).random())
    }
}

fun FlowContent.lightCell(eventId: EventId) {
    row {
        setData(StarLightKey.EventLightId, eventId)
        starLight((0..10).random())
    }
}