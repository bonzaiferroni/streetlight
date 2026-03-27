package streetlight.web.ui

import koala.core.queryFirstOrNull
import koala.css.StyleProperty
import koala.css.UrlValue
import koala.dom.*
import koala.html.IconElement
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import streetlight.model.data.EventId
import streetlight.model.data.EventStar
import streetlight.model.data.StarType
import streetlight.web.GalaxyPathIdRoute
import streetlight.web.model.Streetlight
import streetlight.web.layouts.EventKey
import streetlight.web.shells.GalaxyProfileKey
import streetlight.web.shells.GalaxyProfileContent
import streetlight.web.shells.galaxyProfileShell
import streetlight.web.layouts.iconPath

fun RenderContext.viewGalaxyProfile(app: Streetlight, content: GalaxyProfileContent) {
    val config = app.config
    val state = storeOf(GalaxyProfileState())

    val isMapVisibleFlow = state.flow.mapDistinct { it.isMapVisible }
    val swapIdFlow = isMapVisibleFlow.mapDistinct { isVisible ->
        when (isVisible) {
            true -> GalaxyProfileKey.MapId
            else -> GalaxyProfileKey.HeaderId
        }
    }

    fun setIsMapVisible(value: Boolean) = state.set { it.copy(isMapVisible = value) }

    val root = shellBox(GalaxyProfileKey.ShellId, app.geoMap, app.appScope) {
        galaxyProfileShell(content)
    }

    queryAndWireSwitch(root, GalaxyProfileKey.MapSwitchId, onToggle = ::setIsMapVisible, bindFlow = isMapVisibleFlow)
    queryAndWireSwapBlock(root, GalaxyProfileKey.SwapId, bindFlow = swapIdFlow)
    wireStarSetters(app, root)

    app.streetMap.setPosts(content.posts)
}

data class GalaxyProfileState(
    val isMapVisible: Boolean = false
)

fun ViewContext<Streetlight>.viewGalaxyProfileRoute() {
    routeBlock<GalaxyPathIdRoute, GalaxyProfileContent>(model.portal, { route ->
        val galaxy = model.client.api.readGalaxy(route.pathId) ?: return@routeBlock null
        val posts = model.client.api.readPosts(galaxy.galaxyId) ?: return@routeBlock null
        val galaxies = model.client.api.readGalaxies() ?: emptyList()
        GalaxyProfileContent(
            galaxy = galaxy,
            posts = posts,
            galaxies = galaxies,
        )
    }) { content ->
        viewGalaxyProfile(model, content)
    }
}

fun RenderContext.wireStarSetters(app: Streetlight, root: HTMLElement) {
    val eventStarCache = app.userCache.eventStar
    val pairs = root.queryAttributeAll(EventKey.StarEventId) { EventId(it) }
    val starMap = mutableMapOf<EventId, EventStar>()

    fun getStar(eventId: EventId) = starMap[eventId] ?: EventStar(eventId, null)

    renderScope.launch {
        eventStarCache.starFlow.collect { stars ->
            pairs.forEach { (element, eventId) ->
                val isStarred = stars.any { it.eventId == eventId }
                val star = when(isStarred) {
                    true -> EventStar(eventId, StarType.Star)
                    else -> EventStar(eventId, null)
                }

                element.queryFirstOrNull(IconElement.Class)?.style
                    ?.setProperty(StyleProperty.maskUrl.to(UrlValue(star.value.iconPath)))

                starMap[eventId] = star
            }
        }
    }

    pairs.forEach { (element, eventId) ->
        element.onClick {
            val star = getStar(eventId).let { star ->
                star.copy(value = when (star.value) {
                    null -> StarType.Star
                    else -> null
                })
            }

            eventStarCache.editStar(star)
        }
    }
}