package streetlight.web.ui

import koala.css.AlignItemsStretch
import koala.css.Flex1
import koala.css.MinHeight8
import koala.css.modify
import koala.dom.*
import koala.model.mapDistinct
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import streetlight.model.data.LocationEdit
import streetlight.model.data.toEdit
import streetlight.web.CreateLocationRoute
import streetlight.web.EditLocationDataRoute
import streetlight.web.EditLocationIdRoute
import streetlight.web.EditLocationRoute
import streetlight.web.model.Streetlight
import streetlight.web.model.LocationEditor

fun RenderContext.viewLocationEditor(
    location: LocationEdit,
    app: Streetlight,
    bindFlow: Flow<LocationEdit>?,
    onEdit: ((LocationEdit) -> Unit)?
) {
    val model = LocationEditor(location, renderScope, app.client)
    val nameFlow = model.editFlow.mapDistinct { it.name }
    val imageUrlFlow = model.editFlow.mapDistinct { it.imageUrl }
    val linkFlow = model.editFlow.mapDistinct { it.website }
    val eventsLinkFlow = model.editFlow.mapDistinct { it.eventsUrl }

    onEdit?.let {
        renderScope.launch {
            model.editFlow.collect {
                onEdit(it)
            }
        }
    }

    bindFlow?.let {
        renderScope.launch {
            it.collect { edit ->
                model.setEdit(edit)
            }
        }
    }

    column(modify(AlignItemsStretch)) {
        imageChoice(
            modifiers = modify(MinHeight8),
            onUpload = { app.client.api.uploadImage(it) },
            onValueChanged = model::setImageUrl,
            urlFlow = imageUrlFlow,
            choicesFlow = app.userCache.file.flow
        )
        textField("name", modify(), model::setPlaceName, nameFlow)
        textField("address", modify(), model::setAddress, model.editFlow.mapDistinct { it.address })
        textField("description", modify(), model::setDescription, model.editFlow.mapDistinct { it.description })
        textField("website", modify(Flex1), model::setLink, linkFlow)
        textField("calendar", modify(), model::setEventsLink, eventsLinkFlow)
        // placeEditor(location.geoPoint, app, model)
//        button("cancel", onClick = app.portal::goBack)
//        button("save", modify(Accent), onClick = {
//            renderScope.launch {
//                model.saveLocation()
//                app.portal.goBack()
//            }
//        })
    }
}

fun RenderContext.viewEditLocationRoute(app: Streetlight) {
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
        viewLocationEditor(it, app, null, null)
    }
    appFooter()
}