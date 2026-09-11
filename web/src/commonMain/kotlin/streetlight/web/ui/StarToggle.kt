package streetlight.web.ui

import koala.Svg
import koala.SvgFile
import koala.css.*
import koala.html.*
import koala.interop.JsSignature
import koala.interop.ThisElement
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.onClick
import streetlight.model.data.Event
import streetlight.model.data.EventLocation
import streetlight.model.data.Galaxy
import streetlight.model.data.ToggleType
import streetlight.model.data.Location
import streetlight.web.layouts.CellContent
import kotlin.uuid.Uuid

fun FlowContent.starToggle(
    toggleType: ToggleType,
    isLit: Boolean,
    uuid: Uuid,
    starCount: Int?,
    unlitSvg: Svg = SvgFile.Star,
    litSvg: Svg = SvgFile.StarFilled,
    signature: JsSignature = StarToggle.ToggleAny,
    mod: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    row(modify(AlignItemsCenter, Gap2Px)) {
        addModifiers(mod, StarToggle.Class)
        setAttribute(Attribute.IsOn.to(isLit))
        setAttribute(StarToggle.TypeData.to(toggleType))
        setAttribute(StarToggle.ToggleId.to(uuid))
        onClick = signature.invokeJs(ThisElement)

        block()
        box {
            icon(unlitSvg, modify(CellContent.ButtonIconMod, StarToggle.UnlitIcon))
            icon(litSvg, modify(CellContent.ButtonIconMod, StarToggle.LitIcon))
        }
        starCount?.let {
            textBlock(it.toString(), modify(CellContent.TextMod, StarToggle.Counter, UserSelectNone))
        }
    }
}

fun FlowContent.starToggle(galaxy: Galaxy) {
    starToggle(ToggleType.Galaxy, galaxy.isLit, galaxy.galaxyId.value, galaxy.starCount, signature = StarToggle.ToggleGalaxy)
}

fun FlowContent.starToggle(event: EventLocation) {
    starToggle(ToggleType.Event, event.isLit, event.eventId.value, event.lightCount, SvgFile.CalendarPlus, SvgFile.CalendarMinus)
}

fun FlowContent.starToggle(event: Event) {
    starToggle(ToggleType.Event, event.isLit, event.eventId.value, event.lightCount, SvgFile.CalendarPlus, SvgFile.CalendarMinus)
}

fun FlowContent.starToggle(location: Location) {
    starToggle(ToggleType.Location, location.isLit, location.locationId.value, location.lightCount)
}

object StarToggle {
    val Class = Class("light-control")
    val Counter = Class("light-counter")
    val Lit = Class("lit")
    val LitIcon = Class("lit-icon")
    val UnlitIcon = Class("unlit-icon")

    val ToggleAny = JsSignature("toggleAny")
    val ToggleGalaxy = JsSignature("toggleGalaxy")

    val TypeData = enumAttributeOf<ToggleType>("light-type")
    val ToggleId = uuidAttributeOf("toggle-id")
}

// language="CSS"
val StarToggleCss get() = with(StarToggle) {"""
    
$Class {
    
    $LitIcon {
        visibility: hidden;
    }
    
    &${Attribute.IsOn.to(true)} {
        color: var(--lamp-fg);
        
        $UnlitIcon {
            visibility: hidden;
        }
        
        $LitIcon {
            visibility: visible;
        }
    }
}
""" }