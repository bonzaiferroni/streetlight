package streetlight.web.ui

import kampfire.model.handleOutcome
import koala.css.*
import koala.dom.*
import koala.html.filigree
import koala.html.heading1
import koala.model.mapDistinctNotNull
import kotlinx.html.js.h3
import streetlight.model.data.Galaxy
import streetlight.model.data.MediumEdit
import streetlight.web.MediumForgeRoute
import streetlight.web.MediumRoute

fun AppScope.viewMediumForge(galaxy: Galaxy?) {
    val model = app.getMediumEditor(MediumEdit(), parentScope)
    goOnRoute(model.stateFlow.mapDistinctNotNull { it.slug?.let { slug -> MediumRoute(slug) }  })

    section(modify(Column)) {
        heading1(galaxy?.name ?: "Profile Post", modify(TextAlignCenter))
        filigree {
            h3("posting content")
        }

        card {
            postForm(model)
        }

        row(modify(JustifyContentEnd)) {
            messageBox(model.message.flow)
            button("Post", onClick = model::submitPost)
        }
    }
}

fun AppScope.viewContentPosterRoute() {
    routeBlock<MediumForgeRoute, Galaxy?>({ route ->
        route.slug?.let { api.readGalaxy(it).handleOutcome(toaster::toast) }
    }) { galaxy ->
        viewMediumForge(galaxy)
    }
}