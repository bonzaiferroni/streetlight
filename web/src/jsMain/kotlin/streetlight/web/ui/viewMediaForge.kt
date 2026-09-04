package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.filigree
import koala.html.heading2
import koala.model.FetcherContent
import koala.model.dedupNotNull
import koala.model.toContentOrNull
import streetlight.model.data.Galaxy
import streetlight.model.data.MediaEdit
import streetlight.model.ui.MediaForgeRoute
import streetlight.model.ui.MediaRoute

fun ViewScope.viewMediaForge(galaxy: Galaxy?) {
    val model = app.getMediaEditor(MediaEdit(), contentScope)
    goOnRoute(model.stateFlow.dedupNotNull { it.slug?.let { slug -> MediaRoute(slug) }  })

    section(modify(FlexColumn)) {
        filigree {
            heading2("Create a Post", modify(TextAlignCenter))
        }

        tabs {
            tab("Content") {
                mediaForm(model)
            }
            tab("Theme") {
                themeForm(model.designer.theme)
            }
            tab("Layout") {
                layoutBuilder(model.designer.layout)
            }
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