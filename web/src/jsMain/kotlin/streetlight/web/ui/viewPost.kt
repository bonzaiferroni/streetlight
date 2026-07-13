package streetlight.web.ui

import kampfire.model.handleOutcome
import koala.dom.AppScope
import koala.dom.replaceDynamicRender
import koala.dom.routeBlock
import koala.dom.shellBox
import streetlight.model.data.Media
import streetlight.model.data.SpaceType
import streetlight.model.ui.MediaRoute
import streetlight.web.io.TalkLog
import streetlight.web.shells.PostKey
import streetlight.web.shells.mediaShell

fun AppScope.viewMedia(media: Media) {
    val root = shellBox(PostKey.ShellId, hookInitializers) {
        mediaShell(media)
    }

    replaceDynamicRender(PostKey.TalkId) {
        val talkLog = TalkLog(parentScope, media.mediaId.value, SpaceType.Post, api)
        viewTalkLog(talkLog)
    }
}

fun AppScope.viewPostRoute() {
    routeBlock<MediaRoute, Media>(portal, { route ->
        readIsland<Media>(PostKey.IslandId) { it.slug == route.slug }
            ?: api.readMedia(route.slug).handleOutcome(toaster::toast)
    }) { post ->
        viewMedia(post)
    }
}