package streetlight.web.layouts

import kabinet.utils.format
import kabinet.utils.toAgoFormat
import kabinet.utils.toMetricString
import kabinet.utils.toRelativeDayFormat
import kabinet.utils.toTimeFormat
import kampfire.api.Username
import kampfire.model.Url
import kampfire.model.toUrl
import koala.Svg
import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.onClick
import streetlight.model.data.Event
import streetlight.model.data.EventEdit
import streetlight.model.data.EventLocation
import streetlight.model.data.ExtraLink
import streetlight.model.data.Galaxy
import streetlight.model.data.GalaxyPost
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.web.ui.postMenu
import streetlight.web.ui.starLightCell
import kotlin.time.Instant

object CellContent {
    val Container = Class("cell-content")
    val CellMod = modify(AlignItemsCenter, CardBg, Gap0, Padding1)
    val DualCellMod = modify(GapTiny, FlexItems1)
    val IconMod = modify(Height3, MarginRight4Px, ColorSchemeBg)
    val ButtonIconMod = modify(Height3, OpacityHigh)
    val ThumbMod = modify(Height3, Aspect1, BorderRadius2, MarginRight4Px)
    val TextMod = modify(SmallText, SingleLine, TextOverflowEllipses, Flex1)
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
    modifiers: ModifierSet? = null,
    block: FlowContent.() -> Unit = {}
) {
    row(modify(modifiers, MinHeight4, MinWidth16, FlexWrap, FlexItems1, GapTiny, TextAlignCenter, MoonShadow)) {
        block()
    }
}

fun FlowContent.cell(
    svg: Svg? = null,
    text: String? = null,
    label: String? = null,
    minWidth: Modifier? = MinWidth16,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    box(modify(minWidth)) {
        row(modify(CellContent.CellMod, modifiers)) {
            cellContent(svg, text, label, block)
        }
    }
}

fun FlowContent.buttonsCell(
    minWidth: Modifier? = MinWidth16,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {},
) {
    box(modify(modifiers, minWidth, CardBg)) {
        row(modify(AlignItemsCenter, JustifyContentSpaceAround, Padding1, Gap2), block)
    }
}

fun FlowContent.cellButton(
    svg: Svg,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {},
) {
    icon(svg, modify(CellContent.ButtonIconMod, modifiers)) {
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
            navigation(url.value, modify(Box, MinWidth12)) {
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

fun FlowContent.starCell(username: Username?) = cell(SvgFile.SomeoneSmall, username?.value ?: "Guest")

fun FlowContent.textPropertyCell(property: String, value: String) {
    cell {
        textBlock(value, modify(CellContent.TextMod, MarginLeft1))
    }
}

fun FlowContent.postedAtCell(postedAt: Instant) {
    cell(SvgFile.Clock, postedAt.toAgoFormat())
}

fun FlowContent.linkCell(link: ExtraLink) {
    linkCell(link.url.toUrl(), SvgFile.Link, link.label)
}

fun FlowContent.moreButton() {
    cellButton(SvgFile.ExpandBelow) {
        onClick = KoalaFun.ToggleAncestor.invoke(ThisElement, FeedProto.Base, FeedPost.ToggleExpand)
    }
}

fun locationCells(location: Location): FlowContent.() -> Unit = {
    // starCell(location.username)
    val mapType = location.mapType ?: "Location"
    cell(SvgFile.MapPinOutline, mapType)
    buttonsCell {
        starLightCell(location)
        moreButton()
    }
}

fun locationCells(username: Username?, edit: LocationEdit): FlowContent.() -> Unit = {
    starCell(username)
    // exampleLightCell()
}

fun eventCells(event: EventEdit): FlowContent.() -> Unit = {
    when (val startsAt = event.startsAt) {
        null -> exampleStartsAtCell()
        else -> startsAtCell(startsAt)
    }
    costCell(event.cost, event.url?.toUrl())
    // exampleLightCell()
}

fun eventCells(event: EventLocation, post: GalaxyPost?): FlowContent.() -> Unit = {
    dateCell(event.startsAt)
    startsAtCell(event.startsAt)
    costCell(event.cost, event.url?.toUrl())
//    event.city?.let {
//        cell(SvgFile.City, it)
//    }
    event.locationName?.let {
        cell(SvgFile.MapPinOutline, it)
    }
    buttonsCell(MinWidth32) {
        post?.let {
            postLight(post)
        }
        starLightCell(event)
        moreButton()
        post?.let {
            postMenu(post.slug, post.username)
        }
    }
}

fun eventCells(event: Event): FlowContent.() -> Unit = {
    dateCell(event.startsAt)
    startsAtCell(event.startsAt)
    costCell(event.cost, event.website?.toUrl())
    buttonsCell {
        starLightCell(event)
        moreButton()
    }
}

fun galaxyCells(galaxy: Galaxy): FlowContent.() -> Unit = {
    cell(SvgFile.Calendar, galaxy.eventCount.toMetricString())
    starLightCell(galaxy)
}