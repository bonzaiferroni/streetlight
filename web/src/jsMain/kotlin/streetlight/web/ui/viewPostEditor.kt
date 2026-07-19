package streetlight.web.ui

import kampfire.model.handleResponse
import koala.css.*
import koala.dom.*
import koala.dom.routeBlock
import koala.html.AppRoute
import koala.html.heading1
import koala.model.tapNotNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import streetlight.model.data.Media
import streetlight.model.data.MediaEdit
import streetlight.model.data.toEdit
import streetlight.model.ui.MediaUpdateRoute
import streetlight.model.ui.MediaRoute
import streetlight.web.model.MediaEditor

fun ViewScope.viewMediumUpdater(model: MediaEditor) {
    val routeFlow = model.stateFlow.tapNotNull { it.slug?.let { slug -> MediaRoute(slug) } }
    goOnRoute(routeFlow)

    section(modify(Column)) {
        heading1("Edit Post", modify(TextAlignCenter))

        card {
            mediaForm(model)
        }

        row(modify(JustifyContentEnd)) {
            button("Edit", onClick = model::submitPost)
        }

        appFooter("")
    }
}

fun RouteScope.viewMediumUpdaterRoute() {
    routeBlock<MediaUpdateRoute, MediaEdit>({ inflator.contentFor<Media>(it)?.toEdit() }) {
        val editor = MediaEditor(it, scope, api, toaster)
        viewMediumUpdater(editor)
    }
}

fun ViewScope.goOnRoute(routeFlow: Flow<AppRoute>) {
    scope.launch {
        routeFlow.collect { route ->
            portal.go(route)
        }
    }
}