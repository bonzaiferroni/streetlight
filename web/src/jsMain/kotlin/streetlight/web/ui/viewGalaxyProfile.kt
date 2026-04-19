package streetlight.web.ui

import koala.dom.*
import koala.model.mapDistinct
import koala.model.storeOf
import streetlight.web.GalaxySlugRoute
import streetlight.web.model.Streetlight
import streetlight.web.shells.GalaxyProfileKey
import streetlight.web.shells.GalaxyProfileContent
import streetlight.web.shells.galaxyProfileShell

fun ViewContext<Streetlight>.viewGalaxyProfile(content: GalaxyProfileContent) {
    val app = model
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

    // queryAndWireSwitch(root, GalaxyProfileKey.MapSwitchId, onToggle = ::setIsMapVisible, bindFlow = isMapVisibleFlow)
    queryAndWireSwapBlock(root, GalaxyProfileKey.SwapId, bindFlow = swapIdFlow)
    wireLights(
        root = root,
        attribute = StarLightKey.EventLightId,
        cache = app.cache.eventLights
    )
    wireGalaxyMenu(app, root, content.galaxy)

    app.streetMap.setPosts(content.listing.events)
}

data class GalaxyProfileState(
    val isMapVisible: Boolean = false
)

fun ViewContext<Streetlight>.viewGalaxyProfileRoute() {
    routeBlock<GalaxySlugRoute, GalaxyProfileContent>(model.portal, { route ->
        val galaxy = model.client.api.readGalaxy(route.slug) ?: return@routeBlock null
        val listing = model.client.api.readPosts(galaxy.galaxyId) ?: return@routeBlock null
        GalaxyProfileContent(
            galaxy = galaxy,
            listing = listing,
        )
    }) { content ->
        viewContextOf(model) {
            viewGalaxyProfile(content)
        }
    }
}