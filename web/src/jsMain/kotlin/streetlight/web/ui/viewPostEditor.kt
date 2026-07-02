package streetlight.web.ui

import kampfire.model.handleOutcome
import koala.css.*
import koala.dom.*
import koala.dom.routeBlock
import koala.html.AppRoute
import koala.html.heading1
import koala.model.mapDistinctNotNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import streetlight.model.data.Medium
import streetlight.model.data.MediumEdit
import streetlight.model.data.PostEdit
import streetlight.model.data.toEdit
import streetlight.web.MediumUpdateRoute
import streetlight.web.MediumRoute
import streetlight.web.model.MediumEditor

fun AppScope.viewMediumUpdater(model: MediumEditor) {
    val routeFlow = model.stateFlow.mapDistinctNotNull { it.slug?.let { slug -> MediumRoute(slug) } }
    goOnRoute(routeFlow)

    section(modify(Column)) {
        heading1("Edit Post", modify(TextAlignCenter))

        card {
            postForm(model)
        }

        row(modify(JustifyContentEnd)) {
            button("Edit", onClick = model::submitPost)
        }

        appFooter("")
    }
}

fun AppScope.viewMediumUpdaterRoute() {
    routeBlock<MediumUpdateRoute, MediumEdit>(
        portal = portal,
        provideData = { route ->
            api.readMedium(route.slug).handleOutcome(toaster::toast) { it.toEdit() }
        }
    ) {
        val editor = MediumEditor(it, parentScope, api, toaster)
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