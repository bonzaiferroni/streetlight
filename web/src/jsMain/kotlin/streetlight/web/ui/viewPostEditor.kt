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
import streetlight.model.data.PostEdit
import streetlight.model.data.BasicPost
import streetlight.model.data.toEdit
import streetlight.web.PostUpdateRoute
import streetlight.web.PostRoute
import streetlight.web.model.PostEditor

fun AppScope.viewPostUpdater(model: PostEditor) {
    val routeFlow = model.stateFlow.mapDistinctNotNull { it.slug?.let { slug -> PostRoute(slug) } }
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

fun AppScope.viewEditPostRoute() {
    routeBlock<PostUpdateRoute, PostEdit>(portal, { route ->
        api.readPost(route.slug).handleResponse(toaster::toast) {
            (it as? BasicPost)?.toEdit()
        }
    }) {
        val editor = PostEditor(it, parentScope, api, toaster)
        viewPostUpdater(editor)
    }
}

fun AppScope.goOnRoute(routeFlow: Flow<AppRoute>) {
    parentScope.launch {
        routeFlow.collect { route ->
            portal.go(route)
        }
    }
}