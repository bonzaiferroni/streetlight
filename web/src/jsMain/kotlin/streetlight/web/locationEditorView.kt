package streetlight.web

import koala.css.Accent
import koala.css.AlignItemsStretch
import koala.css.MinHeight8
import koala.css.Width100
import koala.css.modify
import koala.dom.*
import koala.model.mapDistinct
import kotlinx.coroutines.launch
import streetlight.model.data.LocationEdit
import streetlight.model.data.toEdit

fun RenderContext.locationEditorView(location: LocationEdit, app: AppContext) {
    val model = LocationEditor(location, renderScope, app.client)
    val nameFlow = model.editFlow.mapDistinct { it.name }
    val imageUrlFlow = model.editFlow.mapDistinct { it.imageUrl }

    column(modify(AlignItemsStretch)) {
        imageChoice(
            modifiers = modify(MinHeight8),
            onUpload = { app.client.api.uploadFile(it) },
            onValueChanged = model::setImageUrl,
            urlFlow = imageUrlFlow,
            choicesFlow = app.userCache.files.flow
        )
        textField("name", modify(Width100), model::setPlaceName, nameFlow)
        placeEditor(app, model)
        button("back", onClick = app.portal::goBack)
        button("save", modify(Accent), onClick = {
            renderScope.launch {
                model.saveLocation()
                app.portal.goBack()
            }
        })
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
                is EditLocationIdRoute -> api.readLocation(route.locationId)?.toEdit()
                is CreateLocationRoute -> LocationEdit()
            }
        }
    ) {
        locationEditorView(it, app)
    }
}