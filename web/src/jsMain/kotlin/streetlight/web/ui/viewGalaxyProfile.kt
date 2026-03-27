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
import streetlight.model.data.EventStar
import streetlight.model.data.InterestType
import streetlight.web.GalaxyPathIdRoute
import streetlight.web.model.Streetlight
import streetlight.web.layouts.EventAttributes
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
    wireInterestControls(app, root)

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

fun RenderContext.wireInterestControls(app: Streetlight, root: HTMLElement) {
    root.wireByAttribute<EventStar>(EventAttributes.interest) { element, interest ->
        var interest = interest

        element.onClick {
            interest = interest.copy(
                value = if (interest.value == null) InterestType.Star else null
            )

            app.userInterest.editEventInterest(interest)
        }

        val iconElement by lazy { element.queryFirstOrNull(IconElement.Class) }
        renderScope.launch {
            app.userInterest.interestFlow.collect { updatedInterest ->
                if (updatedInterest.eventId != interest.eventId) return@collect
                interest = updatedInterest

                val element = iconElement ?: return@collect
                element.style.setProperty(StyleProperty.maskUrl.to(UrlValue(interest.value.iconPath)))
            }
        }
    }
}