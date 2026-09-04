package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.dom.routeBlock
import koala.html.AppRoute
import koala.html.heading1
import koala.model.dedupNotNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import streetlight.model.data.Media
import streetlight.model.data.toEdit
import streetlight.model.ui.MediaUpdateRoute
import streetlight.model.ui.MediaRoute
import streetlight.web.model.MediaEditor

fun ViewScope.viewMediaConfig(model: MediaEditor) {
    val routeFlow = model.stateFlow.dedupNotNull { it.slug?.let { slug -> MediaRoute(slug) } }
    goOnRoute(routeFlow)

    section(modify(FlexColumn)) {
        heading1("Edit Post", modify(TextAlignCenter))

        mediaForm(model)

        row(modify(JustifyContentEnd)) {
            button("Edit", onClick = model::submitPost)
        }

        appFooter("")
    }
}

fun RouteScope.viewMediumUpdaterRoute() {
    routeBlock<MediaUpdateRoute, Media> {
        val editor = MediaEditor(it.toEdit(), contentScope, api, toaster)
        viewMediaConfig(editor)
    }
}

fun ViewScope.goOnRoute(routeFlow: Flow<AppRoute>) {
    contentScope.launch {
        routeFlow.collect { route ->
            portal.go(route)
        }
    }
}