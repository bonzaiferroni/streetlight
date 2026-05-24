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
import streetlight.model.data.Post
import streetlight.model.data.toEdit
import streetlight.web.EditPostRoute
import streetlight.web.PostRoute
import streetlight.web.model.PostEditor

fun RenderContext.viewPostUpdater(model: PostEditor) {
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

fun RenderContext.viewEditPostRoute() {
    routeBlock<EditPostRoute, PostEdit>(portal, { route ->
        api.readPost(route.postId).handleResponse(toaster::toast) {
            (it as? Post)?.toEdit()
        }
    }) {
        val editor = PostEditor(it, renderScope, api, toaster)
        viewPostUpdater(editor)
    }
}

fun RenderContext.goOnRoute(routeFlow: Flow<AppRoute>) {
    renderScope.launch {
        routeFlow.collect { route ->
            portal.go(route)
        }
    }
}