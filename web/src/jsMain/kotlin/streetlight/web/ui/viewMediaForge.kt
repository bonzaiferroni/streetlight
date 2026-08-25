package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.filigree
import koala.html.heading1
import koala.model.FetcherContent
import koala.model.dedupNotNull
import koala.model.toContentOrNull
import kotlinx.html.js.h3
import streetlight.model.data.Galaxy
import streetlight.model.data.MediaEdit
import streetlight.model.ui.MediaForgeRoute
import streetlight.model.ui.MediaRoute

fun ViewScope.viewMediaForge(galaxy: Galaxy?) {
    val model = app.getMediaEditor(MediaEdit(), contentScope)
    goOnRoute(model.stateFlow.dedupNotNull { it.slug?.let { slug -> MediaRoute(slug) }  })

    section(modify(Column)) {
        heading1(galaxy?.name ?: "Profile Post", modify(TextAlignCenter))
        filigree {
            h3("posting content")
        }

        card {
            mediaForm(model)
        }

        row(modify(JustifyContentEnd)) {
            messageBox(model.message)
            button("Post", onClick = {
                model.submitPost(galaxy)
            })
        }
    }
}

fun RouteScope.viewContentPosterRoute() {
    routeBlock<MediaForgeRoute, FetcherContent> { content ->
        viewMediaForge(content.toContentOrNull<Galaxy>())
    }
}