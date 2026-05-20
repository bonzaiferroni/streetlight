package streetlight.web.ui

import kampfire.model.handleResponse
import koala.LottieFile
import koala.css.*
import koala.dom.*
import streetlight.model.data.Galaxy
import streetlight.model.data.LocationEdit
import streetlight.web.LocationScoutRoute
import streetlight.web.model.LocationEditor
import streetlight.web.model.LocationScout

fun RenderContext.viewLocationScout(galaxy: Galaxy) {
    // val model = app.getCoroutineScoped<GalaxyEditor>(null, renderScope)
    val editor = app.getCoroutineScoped<LocationEditor>(LocationEdit(), renderScope)
    val model = app.getCoroutineScoped<LocationScout>(galaxy, editor, renderScope)

    column {
        introSection("Location Scout", lottie = LottieFile.StrollingMan) {
            textBlock("Let's post a location.")
        }

        flowBlock(model.isEditorStaged, modify(Magic, Blur)) { isEditorStaged ->
            when (isEditorStaged) {
                true -> formBody {
                    locationDetailsForm(editor)
                    locationImageForm(editor)
                    locationLinksForm(editor)
                }
                else -> formBody {
                    locationFinderForm(model)
                }
            }
        }

        appFooter("")
    }
}

fun RenderContext.viewLocationScoutRoute() {
    routeBlock<LocationScoutRoute, Galaxy>({
        api.readGalaxy(it.slug).handleResponse(toaster::toast)
    }) { galaxy ->
        viewLocationScout(galaxy)
    }
}