package streetlight.web.ui

import koala.css.Accent
import koala.css.AlignItemsStretch
import koala.css.Flex1
import koala.css.MinHeight8
import koala.css.modify
import koala.dom.*
import koala.model.mapDistinct
import kotlinx.coroutines.launch
import streetlight.model.data.LocationEdit
import streetlight.model.data.toEdit
import streetlight.web.CreateLocationRoute
import streetlight.web.EditLocationDataRoute
import streetlight.web.EditLocationIdRoute
import streetlight.web.EditLocationRoute
import streetlight.web.model.AppContext
import streetlight.web.model.LocationEditor

fun RenderContext.locationEditorView(location: LocationEdit, app: AppContext) {
    val model = LocationEditor(location, renderScope, app.client)
    val nameFlow = model.editFlow.mapDistinct { it.name }
    val imageUrlFlow = model.editFlow.mapDistinct { it.imageUrl }
    val linkFlow = model.editFlow.mapDistinct { it.link }
    val eventsLinkFlow = model.editFlow.mapDistinct { it.eventsLink }

    column(modify(AlignItemsStretch)) {
        imageChoice(
            modifiers = modify(MinHeight8),
            onUpload = { app.client.api.uploadFile(it) },
            onValueChanged = model::setImageUrl,
            urlFlow = imageUrlFlow,
            choicesFlow = app.userCache.files.flow
        )
        textField("name", modify(), model::setPlaceName, nameFlow)
        row {
            textField("link", modify(Flex1), model::setLink, linkFlow)
            button("🤖 read details from link", onClick = model::parseLocation)
        }
        textField("events", modify(), model::setEventsLink, eventsLinkFlow)
        placeEditor(location.geoPoint, app, model)
        button("cancel", onClick = app.portal::goBack)
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
                is EditLocationIdRoute -> route.locationId?.let { api.readLocation(it)?.toEdit() } ?: LocationEdit()
                is CreateLocationRoute -> LocationEdit()
            }
        }
    ) {
        locationEditorView(it, app)
    }
    appFooter()
}