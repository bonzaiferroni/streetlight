package streetlight.web.ui

import kampfire.model.small
import koala.css.*
import koala.dom.*
import koala.html.btn
import koala.html.filigree
import koala.html.heading1
import koala.model.mapDistinct
import kotlinx.html.js.h3
import kotlinx.html.js.section
import streetlight.model.data.Galaxy
import streetlight.web.GalaxySlugRoute
import streetlight.web.PostContentRoute
import streetlight.web.model.ContentPoster
import streetlight.web.model.Streetlight

fun ViewContext<ContentPoster>.viewContentPoster(galaxy: Galaxy) {
    section {
        heading1(galaxy.name, modify(TextAlignCenter))
        filigree {
            h3("posting content")
        }

        card {
            viewContextOf(model.editor) {
                viewContentEditor()
            }
        }

        row(modify(JustifyContentEnd)) {
            messageBox(model.message.flow)
            button("Post", onClick = model::submitPost)
        }
    }
}

fun ViewContext<Streetlight>.viewContentPosterRoute() {
    routeBlock<PostContentRoute, Galaxy>({
        api.readGalaxy(it.slug)
    }) { galaxy ->
        val model = ContentPoster(renderScope, model, galaxy)
        viewContextOf(model) {
            viewContentPoster(galaxy)
        }
    }
}