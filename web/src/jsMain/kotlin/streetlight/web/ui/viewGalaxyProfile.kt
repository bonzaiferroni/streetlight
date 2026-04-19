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

    val root = shellBox(GalaxyProfileKey.ShellId) {
        galaxyProfileShell(content)
    }

    // queryAndWireSwitch(root, GalaxyProfileKey.MapSwitchId, onToggle = ::setIsMapVisible, bindFlow = isMapVisibleFlow)
    wireLights(
        root = root,
        attribute = StarLightKey.EventLightId,
        cache = app.cache.eventLights
    )
    wireGalaxyMenu(app, root, content.galaxy)

    app.streetMap.setPosts(content.listing.events)
}

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