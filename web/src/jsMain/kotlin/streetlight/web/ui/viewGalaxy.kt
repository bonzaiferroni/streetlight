package streetlight.web.ui

import koala.dom.*
import kotlinx.coroutines.launch
import streetlight.web.GalaxyRoute
import streetlight.web.io.getDataOrNull
import streetlight.web.io.handleResponse
import streetlight.web.layouts.PostKey
import streetlight.web.layouts.layoutPosts
import streetlight.web.model.Streetlight
import streetlight.web.shells.GalaxyKey
import streetlight.web.shells.GalaxyContent
import streetlight.web.shells.galaxyShell

fun ViewContext<Streetlight>.viewGalaxy(content: GalaxyContent) {
    val app = model
    val stage = model.stage.galaxy

    val root = shellBox(GalaxyKey.ShellId) {
        galaxyShell(content)
    }

    // queryAndWireSwitch(root, GalaxyProfileKey.MapSwitchId, onToggle = ::setIsMapVisible, bindFlow = isMapVisibleFlow)
    wireLights(
        root = root,
        attribute = StarLightKey.EventLightId,
        cache = app.cache.eventLights
    )
    wireGalaxyMenu(app, root, content.galaxy)

    app.streetMap.setPosts(content.posts)
    stage.setStage(content)

    renderScope.launch {
        stage.postFlow.collect { posts ->
            val posts = posts.takeIf { !stage.stateNow.isInitialStage } ?: return@collect
            app.streetMap.setPosts(posts)
            replaceRender(PostKey.PostLayoutId, root) {
                column(PostKey.PostLayoutColumnMod) {
                    layoutPosts(posts)
                }
            }
        }
    }
}

fun AppContext.viewGalaxyRoute() {
    routeBlock<GalaxyRoute, GalaxyContent>(model.portal, { route ->
        readIslandOrApi(GalaxyKey.GalaxyContentId, { it.galaxy.galaxyId.value == route.id || it.galaxy.slug == route.id}) {
            val galaxy = api.readGalaxy(route.id).handleResponse(toaster::toast) ?: return@routeBlock null
            val listing = api.readPosts(galaxy.galaxyId).getDataOrNull() ?: return@routeBlock null
            GalaxyContent(
                galaxy = galaxy,
                posts = listing,
            )
        }
    }) { content ->
        viewContextOf(model) {
            viewGalaxy(content)
        }
    }
}