package streetlight.web.view

import koala.modifier.*
import koala.dom.*
import koala.component.filigree
import koala.component.heading2
import koala.model.FetcherContent
import koala.utility.dedupNotNull
import koala.model.toContentOrNull
import streetlight.model.data.Galaxy
import streetlight.model.data.MediaEdit
import streetlight.model.ui.MediaForgeRoute
import streetlight.model.ui.MediaRoute
import koala.dom.component.*
import koala.dom.layout.*
import streetlight.web.model.getMediaEditor

fun ViewScope.viewMediaForge(galaxy: Galaxy?) {
    val model = app.getMediaEditor(MediaEdit(), contentScope)
    goOnRoute(model.stateFlow.dedupNotNull { it.slug?.let { slug -> MediaRoute(slug) }  })

    section(FlexColumn) {
        filigree {
            heading2("Create a Post", TextAlignCenter)
        }

        lazyTabs {
            tab("Content") {
                context(model) {
                    mediaForm()
                }
            }
            tab("Theme") {
                context(model.designer.theme) {
                    themeForm()
                }
            }
            tab("Layout") {
                context(model.designer.layout) {
                    layoutBuilder()
                }
            }
        }

        row(JustifyContentEnd) {
            messageBox(model.message)
            button("Post", onClick = {
                model.submitPost(galaxy)
            })
        }
    }
}

fun RouteScope.viewMediaForgeRoute() {
    routeBlock<MediaForgeRoute, FetcherContent> { content ->
        viewMediaForge(content.toContentOrNull<Galaxy>())
    }
}