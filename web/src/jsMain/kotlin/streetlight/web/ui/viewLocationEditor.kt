package streetlight.web.ui

import koala.css.Accent
import koala.css.AlignItemsStretch
import koala.css.Aspect3By2
import koala.css.BorderRadius1
import koala.css.ContainerMdRow
import koala.css.Flex1
import koala.css.JustifyContentSpaceBetween
import koala.css.QueryContainer
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
import streetlight.web.pages.appFooter

fun ViewContext<LocationEditor>.viewLocationEditor() {
    val nameFlow = model.editFlow.mapDistinct { it.name }
    val linkFlow = model.editFlow.mapDistinct { it.website }
    val eventsLinkFlow = model.editFlow.mapDistinct { it.eventsUrl }
    val imageFlow = model.editFlow.mapDistinct { it.imageRef?.takeIf { url -> url.value.isNotBlank() } }


    column(modify(AlignItemsStretch, QueryContainer)) {
        column(modify(ContainerMdRow)) {
            imageDrop(imageFlow, model::setImageRef, modify(Aspect3By2, BorderRadius1, Flex1))
            column(modify(Flex1)) {
                textField("name", modify(), model::setPlaceName, nameFlow)
                textField("address", modify(), model::setAddress, model.editFlow.mapDistinct { it.address })
                textField("city", modify(), model::setCity, model.editFlow.mapDistinct { it.city })
            }
        }

        textEditor(
            label = "description",
            placeholder = "Location description",
            // 8 rows are default as roughly the desired content length, field can be resized
            rows = 8,
            onValue = model::setDescription,
            flow = model.editFlow.mapDistinct { it.description }
        )

        textField("website", modify(Flex1), model::setLink, linkFlow)
        textField("calendar", modify(), model::setEventsLink, eventsLinkFlow)
    }
}

fun AppContext.viewEditLocationRoute() {
    val app = model
    val portal = app.portal
    val api = app.client.api

    column {
        routeBlock<EditLocationRoute, LocationEdit>(
            portal = portal,
            provideData = { route ->
                when (route) {
                    is EditLocationDataRoute -> route.location
                    is EditLocationIdRoute -> route.locationId?.let { api.readLocation(it)?.toEdit() } ?: LocationEdit()
                    is CreateLocationRoute -> LocationEdit()
                }
            }
        ) { edit ->
            val editor = LocationEditor(edit, renderScope, app)
            viewContextOf(editor) {
                viewLocationEditor()
            }
        }
        appFooter()
    }
}