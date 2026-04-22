package streetlight.web.ui

import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import streetlight.model.data.EventId
import streetlight.model.data.GalaxyId
import streetlight.model.data.LocationId

fun FlowContent.starLight(
    visibility: Int?,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    row {
        addModifiers(modifiers, StarLightKey.Class, Size100P)
        block()
        visibility?.let {
            textBlock(it.toString(), modify(StarLightKey.LightCounter, UserSelectNone))
        }
        icon(SvgFile.LoaderSmall, modify(Height3, Aspect1, MarginLeft1))
    }
}

// td: find a better home
object StarLightKey {
    val Class = Class("star-light")
    val IsLit = Class("is-lit")
    val LightCounter = Class("light-counter")
    val EventLightId = Attribute<EventId>("event-light-id")
    val GalaxyLightId = Attribute<GalaxyId>("galaxy-light-id")
    val LocationLightId = Attribute<LocationId>("location-light-id")

    const val GALAXY_LIGHT_CACHE = "streetlight.galaxy-light-cache"
    const val EVENT_LIGHT_CACHE = "streetlight.event-light-cache"
}

// language="CSS"
val StarLightCss get() = """
${StarLightKey.Class} > ${IconKey.Class} {
    mask-image: url(${SvgFile.Light});
    -webkit-mask-image: url(${SvgFile.Light});
}

${StarLightKey.Class}${StarLightKey.IsLit} > ${IconKey.Class} {
    mask-image: url(${SvgFile.LightFilled});
    -webkit-mask-image: url(${SvgFile.LightFilled});
    animation: var(--glow-background-infinite), var(--glow-shadow-infinite);
}

${StarLightKey.Class}$Magic {
    animation: var(--glow-flash-short);
}
"""