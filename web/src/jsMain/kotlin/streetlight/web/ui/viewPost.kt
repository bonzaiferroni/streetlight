package streetlight.web.ui

import kampfire.model.handleResponse
import koala.dom.RouteScope
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
    val root = shellBox(PostKey.ShellId) {
        mediaShell(media)
    }

    mountChildView(PostKey.TalkId) {
        val talkLog = TalkLog(contentScope, media.mediaId.value, SpaceType.Post, api)
        viewTalkLog(talkLog)
    }
}

fun RouteScope.viewPostRoute() {
    routeBlock<MediaRoute, Media> { post ->
        viewMedia(post)
    }
}