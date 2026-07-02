package streetlight.web.ui

import kampfire.model.handleOutcome
import koala.dom.AppScope
import koala.dom.replaceRender
import koala.dom.routeBlock
import koala.dom.shellBox
import streetlight.model.data.Medium
import streetlight.model.data.SpaceType
import streetlight.web.MediumRoute
import streetlight.web.io.TalkLog
import streetlight.web.shells.PostKey
import streetlight.web.shells.mediumShell

fun AppScope.viewMedia(medium: Medium) {
    val root = shellBox(PostKey.ShellId, hookInitializers) {
        mediumShell(medium)
    }

    replaceRender(PostKey.TalkId) {
        val talkLog = TalkLog(parentScope, medium.mediumId.value, SpaceType.Post, api)
        viewTalkLog(talkLog)
    }
}

fun AppScope.viewPostRoute() {
    routeBlock<MediumRoute, Medium>(portal, { route ->
        readIsland<Medium>(PostKey.IslandId) { it.slug == route.slug }
            ?: api.readMedium(route.slug).handleOutcome(toaster::toast)
    }) { post ->
        viewMedia(post)
    }
}