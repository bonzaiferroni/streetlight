package streetlight.web.ui

import koala.dom.routeBlock
import koala.dom.shellBox
import koala.dom.viewContextOf
import streetlight.model.data.StarPost
import streetlight.web.StarPostRoute
import streetlight.web.io.handleResponse
import streetlight.web.shells.PostKey
import streetlight.web.shells.starPostShell

fun AppContext.viewPost(post: StarPost) {
    val root = shellBox(PostKey.ShellId) {
        starPostShell(post)
    }
}

fun AppContext.viewPostRoute() {
    routeBlock<StarPostRoute, StarPost>(portal, { route ->
        readIslandOrApi(PostKey.IslandId) {
            api.readPost(route.postId).handleResponse(toaster::toast) as? StarPost
        }
    }) { post ->
        viewContextOf(model) {
            viewPost(post)
        }
    }
}