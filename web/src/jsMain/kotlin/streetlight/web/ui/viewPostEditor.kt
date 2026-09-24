package streetlight.web.ui

import koala.modifier.*
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

    section(FlexColumn) {
        heading1("Edit Post", TextAlignCenter)

        mediaForm(model)

        row(JustifyContentEnd) {
            button("Edit", onClick = model::submitPost)
        }

        appFooter(sourcePathUi("viewPostEditor.kt"))
    }
}

fun RouteScope.viewMediumUpdaterRoute() {
    routeBlock<MediaUpdateRoute, Media> {
        val editor = MediaEditor(it.toEdit(), contentScope, api, toaster)
        viewMediaConfig(editor)
    }
}

/** Goes to each route [routeFlow] emits. */
fun ViewScope.goOnRoute(routeFlow: Flow<AppRoute>) {
    contentScope.launch {
        routeFlow.collect { route ->
            portal.go(route)
        }
    }
}