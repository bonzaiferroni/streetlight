package streetlight.web.ui

import koala.Svg
import koala.SvgFile
import koala.css.*
import koala.html.*
import koala.interop.ThisElement
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.onClick
import streetlight.model.data.Event
import streetlight.model.data.EventId
import streetlight.model.data.EventLocation
import streetlight.model.data.Galaxy
import streetlight.model.data.GalaxyId
import streetlight.model.data.StarType
import streetlight.model.data.Location
import streetlight.model.data.LocationId
import streetlight.web.layouts.CellContent
import streetlight.web.layouts.LightControl
import kotlin.uuid.Uuid

fun FlowContent.starLightCell(
    starType: StarType,
    isLit: Boolean,
    uuid: Uuid,
    lightCount: Int?,
    unlitSvg: Svg = SvgFile.Star,
    litSvg: Svg = SvgFile.StarFilled,
    mod: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    row(modify(AlignItemsCenter, Gap2Px)) {
        addModifiers(mod, LightControl.Class, LightControl.getLitMod(isLit))
        setAttribute(LightControl.TypeData.to(starType))
        onClick = LightControl.ToggleFun.invokeJs(ThisElement, uuid)

        block()
        box {
            icon(unlitSvg, modify(CellContent.ButtonIconMod, LightControl.UnlitIcon))
            icon(litSvg, modify(CellContent.ButtonIconMod, LightControl.LitIcon))
        }
        lightCount?.let {
            textBlock(it.toString(), modify(CellContent.TextMod, LightControl.Counter, UserSelectNone))
        }
    }
}

fun FlowContent.starLightCell(galaxy: Galaxy) {
    starLightCell(StarType.Galaxy, galaxy.isLit, galaxy.galaxyId.value, galaxy.starCount)
}

fun FlowContent.starLightCell(event: EventLocation) {
    starLightCell(StarType.Event, event.isLit, event.eventId.value, event.lightCount, SvgFile.CalendarPlus, SvgFile.CalendarMinus)
}

fun FlowContent.starLightCell(event: Event) {
    starLightCell(StarType.Event, event.isLit, event.eventId.value, event.lightCount, SvgFile.CalendarPlus, SvgFile.CalendarMinus)
}

fun FlowContent.starLightCell(location: Location) {
    starLightCell(StarType.Location, location.isLit, location.locationId.value, location.lightCount)
}

// fun FlowContent.exampleLightCell() {
//     cellButtons(SvgFile.StarOutline)
// }

// td: find a better home
object StarLightKey {
    val Class = Class("star-light")
    val IsLit = Class("is-lit")
    val LightCounter = Class("light-counter")
    val EventLightId = uuidAttributeOf("event-light-id") { EventId(it) }
    val GalaxyLightId = uuidAttributeOf("galaxy-light-id") { GalaxyId(it) }
    val LocationLightId = uuidAttributeOf("location-light-id") { LocationId(it) }

    const val GALAXY_LIGHT_CACHE = "streetlight.galaxy-light-cache"
    const val EVENT_LIGHT_CACHE = "streetlight.event-light-cache"
}

// language="CSS"
val StarLightCss get() = """
${StarLightKey.Class} > ${IconStyle.Icon} {
    mask-image: url(${SvgFile.Light});
    -webkit-mask-image: url(${SvgFile.Light});
}

${StarLightKey.Class}${StarLightKey.IsLit} > ${IconStyle.Icon} {
    mask-image: url(${SvgFile.LightFilled});
    -webkit-mask-image: url(${SvgFile.LightFilled});
    animation: var(--glow-background-infinite), var(--glow-shadow-infinite);
}

${StarLightKey.Class}$Magic {
    animation: var(--glow-flash-short);
}
"""