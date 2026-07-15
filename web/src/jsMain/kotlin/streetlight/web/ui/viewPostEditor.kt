package streetlight.web.ui

import kampfire.model.handleResponse
import koala.css.*
import koala.dom.*
import koala.dom.routeBlock
import koala.html.AppRoute
import koala.html.heading1
import koala.model.mapDistinctNotNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import streetlight.model.data.MediaEdit
import streetlight.model.data.toEdit
import streetlight.model.ui.MediaUpdateRoute
import streetlight.model.ui.MediaRoute
import streetlight.web.model.MediaEditor

fun AppScope.viewMediumUpdater(model: MediaEditor) {
    val routeFlow = model.stateFlow.mapDistinctNotNull { it.slug?.let { slug -> MediaRoute(slug) } }
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

fun AppScope.viewMediumUpdaterRoute() {
    routeBlock<MediaUpdateRoute, MediaEdit>(
        portal = portal,
        provideData = { route ->
            api.readMedia(route.slug).handleResponse(toaster) { it.toEdit() }
        }
    ) {
        val editor = MediaEditor(it, parentScope, api, toaster)
        viewMediumUpdater(editor)
    }
}

fun AppScope.goOnRoute(routeFlow: Flow<AppRoute>) {
    parentScope.launch {
        routeFlow.collect { route ->
            portal.go(route)
        }
    }
}