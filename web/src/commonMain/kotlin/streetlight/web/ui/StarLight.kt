package streetlight.web.ui

import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.EventId
import streetlight.model.data.GalaxyId

fun FlowContent.starLight(count: Int, modifiers: ModifierSet? = null) {
    row {
        addModifiers(StarLightKey.Class, modifiers)
        textBlock(count.toString())
        icon(SvgFile.LoaderSmall, modify(Height3, AspectRatio1))
    }
}

// td: find a better home
object StarLightKey {
    val Class = Class("star-light")
    val IsLit = Class("is-lit")
    val EventLightId = Attribute<EventId>("event-light-id")
    val GalaxyLightId = Attribute<GalaxyId>("galaxy-light-id")

    const val GALAXY_LIGHT_CACHE = "streetlight.galaxy-light-cache"
    const val EVENT_LIGHT_CACHE = "streetlight.event-light-cache"
}

// language="CSS"
val StarLightCss get() = """
${StarLightKey.Class} > ${IconKey.Class} {
    mask-image: url(${SvgFile.Light});
    -webkit-mask-image: url(${SvgFile.Light});
}
${StarLightKey.IsLit} > ${StarLightKey.Class} > ${IconKey.Class} {
    mask-image: url(${SvgFile.LightFilled});
    -webkit-mask-image: url(${SvgFile.LightFilled});
    animation: glow-background 10s infinite linear, glow-shadow 10s infinite linear;
}
"""