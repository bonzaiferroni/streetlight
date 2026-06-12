package streetlight.web.ui

import kampfire.model.handleResponse
import koala.dom.AppScope
import koala.dom.replaceRender
import koala.dom.routeBlock
import koala.dom.shellBox
import streetlight.model.data.SpaceType
import streetlight.model.data.BasicPost
import streetlight.web.PostRoute
import streetlight.web.io.TalkLog
import streetlight.web.shells.PostKey
import streetlight.web.shells.postShell

fun AppScope.viewPost(post: BasicPost) {
    val root = shellBox(PostKey.ShellId) {
        postShell(post)
    }

    replaceRender(PostKey.TalkId) {
        val talkLog = TalkLog(parentScope, post.postId.value, SpaceType.Post, api)
        viewTalkLog(talkLog)
    }
}

fun AppScope.viewPostRoute() {
    routeBlock<PostRoute, BasicPost>(portal, { route ->
        readIsland<BasicPost>(PostKey.IslandId) { it.slug == route.slug }
            ?: api.readPost(route.slug).handleResponse(toaster::toast) as? BasicPost
    }) { post ->
        viewPost(post)
    }
}