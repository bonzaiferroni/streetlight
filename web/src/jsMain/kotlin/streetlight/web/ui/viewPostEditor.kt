package streetlight.web.ui

import kampfire.model.handleResponse
import koala.css.*
import koala.dom.*
import koala.dom.routeBlock
import koala.html.heading1
import koala.model.mapDistinct
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import streetlight.model.data.Post
import streetlight.model.data.StarPostEdit
import streetlight.model.data.StarPost
import streetlight.model.data.toEdit
import streetlight.web.EditPostRoute
import streetlight.web.GalaxyRoute
import streetlight.web.model.ContentEditor
import streetlight.web.model.Streetlight

fun RenderContext.viewContentUpdater(model: ContentEditor) {

    section(modify(Column)) {
        heading1("Edit Post", modify(TextAlignCenter))

        card {
            viewContentEditor(model)
        }

        row(modify(JustifyContentEnd)) {
            button("Edit", onClick = model::submitPost)
        }

        appFooter("")
    }

    stagePostAndGo(model.stateFlow.mapDistinct { it.post })
}

fun RenderContext.viewEditPostRoute() {
    routeBlock<EditPostRoute, StarPostEdit>(portal, { route ->
        api.readPost(route.postId).handleResponse(toaster::toast) {
            (it as? StarPost)?.toEdit()
        }
    }) {
        val editor = ContentEditor(it, renderScope, api, toaster)
        viewContentUpdater(editor)
    }
}

fun RenderContext.stagePostAndGo(postFlow: Flow<Post?>) {
    renderScope.launch {
        postFlow.collect { post ->
            if (post != null) {
                stage.addPost(post)
                portal.go(GalaxyRoute(post.galaxyId.toString()))
            }
        }
    }
}