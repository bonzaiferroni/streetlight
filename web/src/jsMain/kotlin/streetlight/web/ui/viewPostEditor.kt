package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.dom.routeBlock
import koala.html.geoMapMount
import koala.html.heading1
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.html.js.section
import streetlight.model.data.ContentEdit
import streetlight.model.data.ContentPost
import streetlight.model.data.LocationEdit
import streetlight.model.data.toEdit
import streetlight.web.EditLocationRoute
import streetlight.web.EditPostRoute
import streetlight.web.GalaxyIdRoute
import streetlight.web.io.handleResponse
import streetlight.web.model.ContentEditor
import streetlight.web.model.Streetlight

fun ViewContext<ContentEditor>.viewPostEditor() {

    section {
        heading1("Edit Post")

        viewContentEditor()

        row(modify(JustifyContentEnd)) {
            button("Edit", onClick = {
                renderScope.launch {
                    api.editPost(model.contentNow).handleResponse(toaster::toast) {
                        stage.galaxy.replacePost(it)
                        portal.go(GalaxyIdRoute(it.galaxyId))
                    }
                }
            })
        }

        appFooter("")
    }
}

fun AppContext.viewEditPostRoute() {
    routeBlock<EditPostRoute, ContentEdit>(portal, { route ->
        api.readPost(route.postId).handleResponse(toaster::toast) {
            (it as? ContentPost)?.toEdit()
        }
    }) {
        val editor = ContentEditor(renderScope, model, it)
        viewContextOf(editor) {
            viewPostEditor()
        }
    }
}