package streetlight.web.ui

import kampfire.model.handleResponse
import koala.css.*
import koala.dom.*
import koala.html.filigree
import koala.html.heading1
import koala.model.mapDistinctNotNull
import kotlinx.html.js.h3
import streetlight.model.data.Galaxy
import streetlight.model.data.MediaEdit
import streetlight.model.ui.MediaForgeRoute
import streetlight.model.ui.MediaRoute

fun AppScope.viewMediaForge(galaxy: Galaxy?) {
    println(galaxy) // ey
    val model = app.getMediaEditor(MediaEdit(), parentScope)
    goOnRoute(model.stateFlow.mapDistinctNotNull { it.slug?.let { slug -> MediaRoute(slug) }  })

    section(modify(Column)) {
        heading1(galaxy?.name ?: "Profile Post", modify(TextAlignCenter))
        filigree {
            h3("posting content")
        }

        card {
            mediaForm(model)
        }

        row(modify(JustifyContentEnd)) {
            messageBox(model.message.flow)
            button("Post", onClick = {
                model.submitPost(galaxy)
            })
        }
    }
}

fun AppScope.viewContentPosterRoute() {
    routeBlock<MediaForgeRoute, Galaxy?>({ route ->
        route.slug?.let { api.readGalaxy(it).handleResponse(toaster::toast) }
    }) { galaxy ->
        viewMediaForge(galaxy)
    }
}