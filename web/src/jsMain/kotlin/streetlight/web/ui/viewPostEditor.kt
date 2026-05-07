package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.dom.routeBlock
import koala.html.heading1
import streetlight.model.data.StarPostEdit
import streetlight.model.data.StarPost
import streetlight.model.data.toEdit
import streetlight.web.EditPostRoute
import streetlight.web.io.handleResponse
import streetlight.web.model.ContentEditor

fun ViewContext<ContentEditor>.viewContentUpdater() {

    section(modify(Column)) {
        heading1("Edit Post", modify(TextAlignCenter))

        card {
            viewContentEditor()
        }

        row(modify(JustifyContentEnd)) {
            button("Edit", onClick = model::submitPost)
        }

        appFooter("")
    }
}

fun AppContext.viewEditPostRoute() {
    routeBlock<EditPostRoute, StarPostEdit>(portal, { route ->
        api.readPost(route.postId).handleResponse(toaster::toast) {
            (it as? StarPost)?.toEdit()
        }
    }) {
        val editor = ContentEditor(renderScope, model, it)
        viewContextOf(editor) {
            viewContentUpdater()
        }
    }
}