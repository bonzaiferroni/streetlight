package streetlight.web.ui

import kampfire.model.small
import koala.css.*
import koala.dom.*
import koala.html.btn
import koala.html.heading4
import koala.html.centeredHeading
import koala.model.mapDistinct
import kotlinx.coroutines.flow.filterNotNull
import streetlight.model.data.Event
import streetlight.model.data.Galaxy
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.web.EventScoutRoute
import streetlight.web.GalaxySlugRoute
import streetlight.web.model.EventScout
import streetlight.web.model.Streetlight

fun RenderContext.viewEventScout(app: Streetlight, galaxy: Galaxy) {
    val model = EventScout(app, renderScope, galaxy)
    val panelFlow = model.stateFlow.mapDistinct {
        EventScoutPanelState(
            locationEdit = it.locationEdit,
            location = it.location,
            event = it.event,
        )
    }

    column {
        geoMapMount(app.geoMap, app.appScope, modifiers = modify(BorderRadius2, MoonShadow, Height48))

        flowBlock(panelFlow, defaultMagic + MoonShadow) {
            val locationEdit = it.locationEdit; val location = it.location; val event = it.event

            viewContextOf(model) {
                if (event != null) {
                    reviewEventPanel(event, galaxy)
                } else if (location != null) {
                    createEventPanel(location)
                } else if (locationEdit != null) {
                    reviewLocationPanel(locationEdit)
                } else {
                    findLocationPanel()
                }
            }
        }

        appFooter("web/src/jsMain/kotlin/streetlight/web/ui/viewEventScout.kt")
    }
}

private data class EventScoutPanelState(
    val locationEdit: LocationEdit? = null,
    val location: Location? = null,
    val event: Event? = null
)

fun ViewContext<Streetlight>.viewEventScoutRoute() {
    routeBlock<EventScoutRoute, Galaxy>({
        api.readGalaxy(it.slug)
    }) { galaxy ->
        viewEventScout(model, galaxy)
    }
}

fun ViewContext<EventScout>.reviewEventPanel(event: Event, galaxy: Galaxy) {
    column {
        card {
            messageBox(model.messageFlow)
            row {
                button("start over", modify(Secondary), onClick = model::reset)
                btn("back to ${galaxy.name}", GalaxySlugRoute(galaxy.slug), galaxy.images.small)
                button("post another event at this location", onClick = model::resetEvent)
            }

//            heading3("Post to a galaxy")
//            row(modify(JustifySpaceBetween)) {
//                textBlock(galaxy.name)
//            }
        }
    }
}

fun ViewContext<EventScout>.createEventPanel(location: Location?) {
    val editFlow = model.stateFlow.mapDistinct { it.eventEdit }
    val linkFlow = model.stateFlow.mapDistinct { it.eventEdit.link }

    column {
        card {
            messageBox(model.messageFlow)
            row {
                textField("website", modify(Flex1), model::setEventLink, linkFlow)
                button("🤖 read website", onClick = model::readEventWebsite)
            }
        }

        card {
            eventEditorForm(model.stateNow.eventEdit, model.app, editFlow.filterNotNull(), model::setEventEdit)
            row(modify(JustifyContentSpaceBetween)) {
                button("start over", onClick = model::reset)
                row {
                    messageBox(model.validEventFlow)
                    button("create", onClick = model::postEvent)
                }
            }
        }
    }
}

fun ViewContext<EventScout>.reviewLocationPanel(locationEdit: LocationEdit) {
    val editFlow = model.stateFlow.mapDistinct { it.locationEdit }.filterNotNull()

    column {
        card {
            messageBox(model.messageFlow)
            row {
                textField("website", modify(Flex1), model::setWebsite, model.stateFlow.mapDistinct { it.website })
                button("🤖 read website", onClick = model::readLocationWebsite)
            }
        }

        card {
            viewLocationEditor(locationEdit, model.app, editFlow, model::setEdit)
            row(modify(JustifyContentSpaceBetween)) {
                button("start over", modify(Secondary), onClick = model::reset)
                button("create location", modify(Accent), onClick = model::postLocation)
            }
        }
    }
}

fun ViewContext<EventScout>.findLocationPanel() {
    val queryFlow = model.stateFlow.mapDistinct { it.query }
    val locationsFlow = model.stateFlow.mapDistinct { it.locations }

    card {
        messageBox(model.messageFlow)
        row(modify(JustifyContentEnd)) {
            textBlock("Move the map target to the location.", modify(Dim))
            button("Here", onClick = model::here)
        }

        centeredHeading("or")

        heading4("Search")
        textBlock("We can search for the location's name, address, city, etc.", modify(Dim))

        row(modify(AlignItemsStart)) {
            column(modify(Flex1, AlignItemsEnd)) {
                textField("search", modify(Width100P), model::setQuery, queryFlow, placeholder = "Search by name or address")
            }
            button("Search", onClick = model::searchQuery)
        }

        itemsBlock(locationsFlow) { location ->
            cardOf(location) {
                model.setLocation(location)
            }
        }
    }
}