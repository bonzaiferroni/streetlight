package streetlight.web.ui

import koala.dom.replaceRender
import koala.dom.routeBlock
import koala.dom.shellBox
import koala.dom.viewContextOf
import streetlight.model.data.SpaceType
import streetlight.model.data.StarPost
import streetlight.web.StarPostRoute
import streetlight.web.io.TalkLog
import streetlight.web.io.handleResponse
import streetlight.web.shells.PostKey
import streetlight.web.shells.starPostShell

fun AppContext.viewStarPost(post: StarPost) {
    val root = shellBox(PostKey.ShellId) {
        starPostShell(post)
    }

    replaceRender(PostKey.TalkId) {
        val talkLog = TalkLog(renderScope, model, post.postId.value, SpaceType.Post)
        viewContextOf(talkLog) {
            viewTalkLog()
        }
    }
}

fun AppContext.viewStarPostRoute() {
    routeBlock<StarPostRoute, StarPost>(portal, { route ->
        readIslandOrApi(PostKey.IslandId, { it.postId.value == route.id || it.slug == route.id }) {
            api.readPost(route.id).handleResponse(toaster::toast) as? StarPost
        }
    }) { post ->
        viewContextOf(model) {
            viewStarPost(post)
        }
    }
}