package streetlight.web.view

import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.mountChildView
import koala.dom.routeBlock
import koala.dom.page.shellBox
import streetlight.model.data.Media
import streetlight.model.data.SpaceType
import streetlight.model.ui.MediaRoute
import streetlight.web.io.TalkLog
import streetlight.web.shell.MediaShell
import streetlight.web.shell.mediaShell
import streetlight.web.model.api
import streetlight.web.dom.page.applyTheme

fun ViewScope.viewMedia(media: Media) {
    shellBox {
        mediaShell(media)
        applyTheme(media.design?.theme)
    }

    mountChildView(MediaShell.TalkId) {
        val talkLog = TalkLog(contentScope, media.mediaId.value, SpaceType.Post, api)
        viewTalkLog(talkLog)
    }
}

fun RouteScope.viewMediaRoute() {
    routeBlock<MediaRoute, Media>(MediaShell.IslandId) { media ->
        viewMedia(media)
    }
}