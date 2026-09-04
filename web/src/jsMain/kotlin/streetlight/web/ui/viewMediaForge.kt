package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.filigree
import koala.html.heading1
import koala.html.heading2
import koala.model.FetcherContent
import koala.model.dedupNotNull
import koala.model.toContentOrNull
import kotlinx.html.js.h3
import streetlight.model.data.DefaultLayout
import streetlight.model.data.Galaxy
import streetlight.model.data.MediaEdit
import streetlight.model.ui.MediaForgeRoute
import streetlight.model.ui.MediaRoute
import streetlight.web.model.LayoutEditor
import streetlight.web.model.ThemeEditor

fun ViewScope.viewMediaForge(galaxy: Galaxy?) {
    val model = app.getMediaEditor(MediaEdit(), contentScope)
    goOnRoute(model.stateFlow.dedupNotNull { it.slug?.let { slug -> MediaRoute(slug) }  })

    section(modify(FlexColumn)) {
        filigree {
            heading2("Create a Post", modify(TextAlignCenter))
        }

        tabs {
            tab("Content") {
                card {
                    mediaForm(model)
                }
            }
            tab("Theme") {
                themeForm(model.themeEditor)
            }
            tab("Layout") {
                layoutBuilder(model.layoutEditor)
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