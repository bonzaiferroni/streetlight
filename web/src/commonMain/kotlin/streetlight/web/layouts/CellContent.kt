package streetlight.web.layouts

import kabinet.utils.format
import kabinet.utils.toAgoFormat
import kabinet.utils.toRelativeDayFormat
import kabinet.utils.toTimeFormat
import kampfire.model.Url
import kampfire.model.toUrl
import koala.Svg
import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.onClick
import streetlight.model.data.EventEdit
import streetlight.model.data.EventId
import streetlight.model.data.EventLocation
import streetlight.model.data.ExtraLink
import streetlight.model.data.Galaxy
import streetlight.model.data.GalaxyId
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.LocationId
import streetlight.web.ui.StarLightKey
import streetlight.web.ui.starLightCell
import kotlin.time.Instant

object CellContent {
    val CellMod = modify(AlignItemsCenter, CardBg, Gap0, Padding1)
    val DualCellMod = modify(GapTiny, FlexItems1)
    val IconMod = modify(Height3, Aspect1, MarginRight4Px, ColorSchemeBg)
    val ThumbMod = modify(Height3, Aspect1, BorderRadius2, MarginRight4Px)
    val TextMod = modify(SmallText, SingleLine, TextOverflowEllipses, Flex1)
}

fun FlowContent.cellBlock(
    modifiers: ModifierSet? = null,
    block: FlowContent.() -> Unit = {}
) {
    row(modify(modifiers, MinHeight4, MinWidth12, FlexItems1, GapTiny, TextAlignCenter, MoonShadow)) {
        block()
    }
}

fun FlowContent.cell(
    svg: Svg? = null,
    text: String? = null,
    label: String? = null,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    box(modify(MinWidth12)) {
        row(modify(CellContent.CellMod, modifiers)) {
            cellContent(svg, text, label, block)
        }
    }
}

fun FlowContent.comboCell(
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    box(modify(MinWidth12)) {
        row(modify(CellContent.DualCellMod, modifiers), block)
    }
}

fun FlowContent.comboCellItem(
    svg: Svg? = null,
    text: String? = null,
    label: String? = null,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    row(modify(CellContent.CellMod, modifiers)) {
        cellContent(svg, text, label, block)
    }
}

fun FlowContent.cellButton(
    svg: Svg,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {},
) {
    box(modify(modifiers, Padding1, PrimaryCardBg, PlaceItemsCenter)) {
        block()
        icon(svg, modify(Height3, OpacityMost))
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
    modifiers: ModifierSet? = null,
    block: FlowContent.() -> Unit = {}
) {
    // cell(modifiers, block)
    when (url) {
        null -> cell(svg, text, modifiers = modifiers, block = block)
        else -> {
            navigation(url.value, modify(MinWidth12)) {
                row(modify(CellContent.CellMod, modifiers)) {
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

fun FlowContent.starCell(username: String?) = cell(SvgFile.SomeoneSmall, username ?: "Someone")

fun FlowContent.textPropertyCell(property: String, value: String) {
    cell {
        textBlock(value, modify(CellContent.TextMod, MarginLeft1))
    }
}

fun FlowContent.postedAtCell(postedAt: Instant) {
    cell(SvgFile.Clock, postedAt.toAgoFormat())
}

fun FlowContent.galaxyLightCell(visibility: Int?, galaxyId: GalaxyId) {
    starLightCell(visibility) {
        setData(StarLightKey.GalaxyLightId, galaxyId)
    }
}

fun FlowContent.eventLightCell(visibility: Int?, eventId: EventId) {
    starLightCell(visibility) {
        setData(StarLightKey.EventLightId, eventId)
    }
}

fun FlowContent.locationLightCell(visibility: Int?, locationId: LocationId) {
    starLightCell(visibility) {
        setData(StarLightKey.LocationLightId, locationId)
    }
}

fun FlowContent.exampleLightCell() {
    starLightCell(0)
}

fun FlowContent.linkCell(link: ExtraLink) {
    linkCell(link.url.toUrl(), SvgFile.Link, link.label)
}

fun FlowContent.moreCell() {
    cellButton(SvgFile.ExpandBelow) {
        onClick = KoalaFun.ToggleAncestor.invoke(ThisElement, FeedPost.Class, FeedPost.ToggleExpand)
    }
}

fun locationCells(location: Location): FlowContent.() -> Unit = {
    starCell(location.username)
    comboCell {
        locationLightCell(location.lightCount, location.locationId)
    }
}

fun locationCells(username: String?, edit: LocationEdit): FlowContent.() -> Unit = {
    starCell(username)
    exampleLightCell()
}

fun eventCells(event: EventEdit): FlowContent.() -> Unit = {
    when (val startsAt = event.startsAt) {
        null -> exampleStartsAtCell()
        else -> startsAtCell(startsAt)
    }
    costCell(event.cost, event.url?.toUrl())
    exampleLightCell()
}

fun eventCells(event: EventLocation): FlowContent.() -> Unit = {
    dateCell(event.startsAt)
    startsAtCell(event.startsAt)
    costCell(event.cost, event.url?.toUrl())
    comboCell {
        eventLightCell(event.lightCount, event.eventId)
        moreCell()
    }
}

fun galaxyCells(galaxy: Galaxy): FlowContent.() -> Unit = {
    cell(SvgFile.Calendar, galaxy.eventCount?.toString() ?: "?")
    galaxyLightCell(galaxy.lightCount, galaxy.galaxyId)
}