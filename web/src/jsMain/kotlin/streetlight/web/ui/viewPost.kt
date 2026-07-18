package streetlight.web.ui

import kampfire.model.handleResponse
import koala.dom.ViewScope
import koala.dom.mountChildView
import koala.dom.routeBlock
import koala.dom.shellBox
import streetlight.model.data.Media
import streetlight.model.data.SpaceType
import streetlight.model.ui.MediaRoute
import streetlight.web.io.TalkLog
import streetlight.web.shells.PostKey
import streetlight.web.shells.mediaShell

fun ViewScope.viewMedia(media: Media) {
    val root = shellBox(PostKey.ShellId, hookInitializers) {
        mediaShell(media)
    }

    mountChildView(PostKey.TalkId) {
        val talkLog = TalkLog(scope, media.mediaId.value, SpaceType.Post, api)
        viewTalkLog(talkLog)
    }
}

fun ViewScope.viewPostRoute() {
    routeBlock<MediaRoute, Media>(portal, { route ->
        readIsland<Media>(PostKey.IslandId) { it.slug == route.slug }
            ?: api.readMedia(route.slug).handleResponse(toaster)
    }) { post ->
        viewMedia(post)
    }
}