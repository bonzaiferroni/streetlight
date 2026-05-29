package streetlight.web.ui

import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import streetlight.model.data.EventId
import streetlight.model.data.GalaxyId
import streetlight.model.data.LocationId
import streetlight.web.layouts.CellContent
import streetlight.web.layouts.comboCellItem

fun FlowContent.starLightCell(
    visibility: Int?,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    comboCellItem {
        addModifiers(modifiers, StarLightKey.Class)
        block()
        icon(SvgFile.LoaderSmall, CellContent.IconMod)
        visibility?.let {
            textBlock(it.toString(), modify(CellContent.TextMod, StarLightKey.LightCounter, UserSelectNone))
        }
    }
}

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