package streetlight.web.ui

import kampfire.model.handleResponse
import koala.css.*
import koala.dom.*
import koala.html.filigree
import koala.html.heading1
import kotlinx.html.js.h3
import streetlight.model.data.StarPostEdit
import streetlight.model.data.Galaxy
import streetlight.web.PostContentRoute
import streetlight.web.model.ContentEditor

fun RenderContext.viewContentPoster(model: ContentEditor, galaxy: Galaxy) {
    section(modify(Column)) {
        heading1(galaxy.name, modify(TextAlignCenter))
        filigree {
            h3("posting content")
        }

        card {
            viewContentEditor(model)
        }

        row(modify(JustifyContentEnd)) {
            messageBox(model.msg.flow)
            button("Post", onClick = model::submitPost)
        }
    }
}

fun RenderContext.viewContentPosterRoute() {
    routeBlock<PostContentRoute, Galaxy>({
        api.readGalaxy(it.slug).handleResponse(toaster::toast)
    }) { galaxy ->
        val model = ContentEditor(StarPostEdit(null, galaxy.galaxyId), renderScope, api, toaster)
        viewContentPoster(model, galaxy)
    }
}