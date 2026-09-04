package streetlight.web.ui

import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.mountChildView
import koala.dom.routeBlock
import koala.dom.shellBox
import streetlight.model.data.Media
import streetlight.model.data.SpaceType
import streetlight.model.ui.MediaRoute
import streetlight.web.io.TalkLog
import streetlight.web.shells.MediaShell
import streetlight.web.shells.mediaShell

fun ViewScope.viewMediaConfig(media: Media) {
    val root = shellBox(MediaShell.ShellId) {
        mediaShell(media)
        applyTheme(media.design?.theme)
    }

    mountChildView(MediaShell.TalkId) {
        val talkLog = TalkLog(contentScope, media.mediaId.value, SpaceType.Post, api)
        viewTalkLog(talkLog)
    }
}

fun RouteScope.viewMediaRoute() {
    routeBlock<MediaRoute, Media>(MediaShell.IslandId) { post ->
        viewMediaConfig(post)
    }
}