package streetlight.web.ui

import kampfire.model.handleResponse
import koala.LottieFile
import koala.dom.*
import streetlight.model.data.Galaxy
import streetlight.model.data.LocationEdit
import streetlight.web.LocationScoutRoute
import streetlight.web.model.LocationEditor

fun RenderContext.viewLocationScout(galaxy: Galaxy) {
    // val model = app.getCoroutineScoped<GalaxyEditor>(null, renderScope)
    val model = app.getCoroutineScoped<LocationEditor>(LocationEdit(), renderScope)

    column {
        introSection("Location Scout", lottie = LottieFile.StrollingMan) {
            textBlock("Let's post a location.")
        }

        formBody {
            locationFinderForm(model)
        }

        formBody {
            locationDetailsForm(model)
            locationImageForm(model)
            locationLinksForm(model)
        }
    }
}

fun RenderContext.viewLocationScoutRoute() {
    routeBlock<LocationScoutRoute, Galaxy>({
        api.readGalaxy(it.slug).handleResponse(toaster::toast)
    }) { galaxy ->
        viewLocationScout(galaxy)
    }
}