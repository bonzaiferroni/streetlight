package streetlight.web.ui

import kampfire.model.handleResponse
import koala.css.*
import koala.dom.*
import koala.html.filigree
import koala.html.heading1
import koala.model.mapDistinctNotNull
import kotlinx.html.js.h3
import streetlight.model.data.PostEdit
import streetlight.model.data.Galaxy
import streetlight.web.PostContentRoute
import streetlight.web.PostRoute

fun RenderContext.viewContentPoster(galaxy: Galaxy) {
    val model = app.getContentEditor(PostEdit(null, galaxy.galaxyId), renderScope)
    goOnRoute(model.stateFlow.mapDistinctNotNull { it.slug?.let { slug -> PostRoute(slug) }  })

    section(modify(Column)) {
        heading1(galaxy.name, modify(TextAlignCenter))
        filigree {
            h3("posting content")
        }

        card {
            postForm(model)
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
        viewContentPoster(galaxy)
    }
}