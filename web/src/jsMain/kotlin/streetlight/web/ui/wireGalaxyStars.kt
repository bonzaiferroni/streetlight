package streetlight.web.ui

import koala.SvgFile
import koala.core.queryFirstOrNull
import koala.css.*
import koala.dom.*
import koala.html.IconKey
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import streetlight.model.data.GalaxyId
import streetlight.web.layouts.GalaxyKey
import streetlight.web.model.Streetlight

fun ViewContext<Streetlight>.wireGalaxyStars(root: HTMLElement) {
    val app = model
    val cache = app.cache.galaxy
    val pairs = root.queryAttributeAll(GalaxyKey.GalaxyStarId) { GalaxyId(it) }

    renderScope.launch {
        cache.starFlow.collect { stars ->
            pairs.forEach { (element, galaxyId) ->
                val isStarred = stars.any { it == galaxyId }
                val icon = when (isStarred) {
                    true -> SvgFile.StarFilled
                    else -> SvgFile.StarOutline
                }

                element.queryFirstOrNull(IconKey.Class)?.style
                    ?.setProperty(Property.MaskUrl.to(UrlValue(icon)))
            }
        }
    }

    pairs.forEach { (element, galaxyId) ->
        element.onClick {
            cache.toggleStar(galaxyId)
        }
    }
}