package streetlight.web.ui

import koala.dom.*
import kotlinx.coroutines.launch
import streetlight.web.GalaxyRoute
import streetlight.web.io.getDataOrNull
import streetlight.web.io.handleResponse
import streetlight.web.layouts.PostKey
import streetlight.web.layouts.layoutPosts
import streetlight.web.model.DataCache
import streetlight.web.model.Streetlight
import streetlight.web.shells.GalaxyKey
import streetlight.web.shells.GalaxyContent
import streetlight.web.shells.galaxyShell

fun RenderContext.viewGalaxy(content: GalaxyContent) {
    val cache = app.get<DataCache>()

    val root = shellBox(GalaxyKey.ShellId) {
        galaxyShell(content)
    }

    // queryAndWireSwitch(root, GalaxyProfileKey.MapSwitchId, onToggle = ::setIsMapVisible, bindFlow = isMapVisibleFlow)
    wireLights(
        root = root,
        attribute = StarLightKey.EventLightId,
        cache = cache.eventLights
    )
    wireGalaxyMenu(root, content.galaxy)

    streetMap.setPosts(content.posts)
    stage.setStage(content)

    renderScope.launch {
        stage.postFlow.collect { posts ->
            val posts = posts.takeIf { !stage.stateNow.isInitialStage } ?: return@collect
            streetMap.setPosts(posts)
            replaceRender(PostKey.PostLayoutId, root) {
                column(PostKey.PostLayoutColumnMod) {
                    layoutPosts(posts)
                }
            }
        }
    }
}

fun RenderContext.viewGalaxyRoute() {
    routeBlock<GalaxyRoute, GalaxyContent>(portal, { route ->
        readIslandOrApi(GalaxyKey.GalaxyContentId, { it.galaxy.galaxyId.toString() == route.id || it.galaxy.slug == route.id}) {
            val galaxy = api.readGalaxy(route.id).handleResponse(toaster::toast) ?: return@routeBlock null
            val listing = api.readPosts(galaxy.galaxyId).getDataOrNull() ?: return@routeBlock null
            GalaxyContent(
                galaxy = galaxy,
                posts = listing,
            )
        }
    }) { content ->
        viewGalaxy(content)
    }
}