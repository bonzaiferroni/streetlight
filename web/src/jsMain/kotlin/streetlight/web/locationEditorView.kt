package streetlight.web

import koala.css.Width100
import koala.css.modify
import koala.dom.*
import koala.model.mapDistinct
import streetlight.model.data.LocationEdit
import streetlight.model.data.toEdit

fun RenderContext.locationEditorView(location: LocationEdit, app: AppContext) {
    val model = LocationEditor(location, renderScope, app.client)
    val nameFlow = model.editFlow.mapDistinct { it.name }

    column {
        textField("name", modify(Width100), model::setPlaceName, nameFlow)
        placeEditor(app, model)
    }
}

fun RenderContext.locationEditorRouteView(app: AppContext) {
    val portal = app.portal
    val api = app.client.api

    routeBlock<EditLocationRoute, LocationEdit>(
        portal = portal,
        provideData = { route ->
            when (route) {
                is EditLocationDataRoute -> route.location
                is EditLocationIdRoute -> route.locationId?.let {
                    api.readLocation(it)?.toEdit()
                } ?: LocationEdit()
            }
        }
    ) {
        locationEditorView(it, app)
    }
}