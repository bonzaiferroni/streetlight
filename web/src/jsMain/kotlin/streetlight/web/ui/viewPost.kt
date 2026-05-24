package streetlight.web.ui

import kampfire.model.handleResponse
import koala.dom.RenderContext
import koala.dom.replaceRender
import koala.dom.routeBlock
import koala.dom.shellBox
import streetlight.model.data.SpaceType
import streetlight.model.data.Post
import streetlight.web.PostRoute
import streetlight.web.io.TalkLog
import streetlight.web.shells.PostKey
import streetlight.web.shells.postShell

fun RenderContext.viewPost(post: Post) {
    val root = shellBox(PostKey.ShellId) {
        postShell(post)
    }

    replaceRender(PostKey.TalkId) {
        val talkLog = TalkLog(renderScope, post.postId.value, SpaceType.Post, api)
        viewTalkLog(talkLog)
    }
}

fun RenderContext.viewPostRoute() {
    routeBlock<PostRoute, Post>(portal, { route ->
        readIsland<Post>(PostKey.IslandId) { it.slug == route.slug }
            ?: api.readPost(route.slug).handleResponse(toaster::toast) as? Post
    }) { post ->
        viewPost(post)
    }
}