package streetlight.web.ui

import kampfire.model.handleResponse
import kampfire.model.small
import koala.css.*
import koala.dom.*
import koala.html.btn
import koala.html.filigree
import koala.html.heading3
import koala.model.mapDistinct
import kotlinx.coroutines.flow.filterNotNull
import streetlight.model.data.Event
import streetlight.model.data.Galaxy
import streetlight.model.data.Location
import streetlight.web.EventScoutRoute
import streetlight.web.GalaxyRoute
import streetlight.web.model.EventScoutProto
import streetlight.web.model.Streetlight

fun RenderContext.viewEventScoutProto(app: Streetlight, galaxy: Galaxy) {
    val model = EventScoutProto(app, renderScope, galaxy)
    val panelFlow = model.stateFlow.mapDistinct {
        EventScoutPanelState(
            locationStaged = it.locationEdit != null,
            location = it.location,
            event = it.event,
        )
    }

    column {
        geoMapMount(app.geoMap, app.appScope, modifiers = modify(BorderRadius2, MoonShadow, Height48))

        flowBlock(panelFlow, defaultMagic) {
            val location = it.location; val event = it.event

            viewContextOf(model) {
                if (event != null) {
                    reviewEventPanel(event, galaxy)
                } else if (location != null) {
                    createEventPanel(location)
                } else if (it.locationStaged) {
                    reviewLocationPanel()
                } else {
                    findLocationPanel()
                }
            }
        }

        appFooter("web/src/jsMain/kotlin/streetlight/web/ui/viewEventScoutProto.kt")
    }
}

private data class EventScoutPanelState(
    val locationStaged: Boolean = false,
    val location: Location? = null,
    val event: Event? = null
)

fun ViewContext<Streetlight>.viewEventScoutProtoRoute() {
    routeBlock<EventScoutRoute, Galaxy>({
        api.readGalaxy(it.slug).handleResponse(toaster::toast)
    }) { galaxy ->
        viewEventScoutProto(model, galaxy)
    }
}

fun ViewContext<EventScoutProto>.reviewEventPanel(event: Event, galaxy: Galaxy) {
    column {
        card {
            messageBox(model.messageFlow)
            row {
                button("start over", modify(Secondary), onClick = model::reset)
                btn("back to ${galaxy.name}", GalaxyRoute(galaxy.slug), galaxy.images.small)
                button("post another event at this location", onClick = model::resetEvent)
            }

//            heading3("Post to a galaxy")
//            row(modify(JustifySpaceBetween)) {
//                textBlock(galaxy.name)
//            }
        }
    }
}

fun ViewContext<EventScoutProto>.createEventPanel(location: Location?) {
    val editFlow = model.stateFlow.mapDistinct { it.eventEdit }
    val linkFlow = model.stateFlow.mapDistinct { it.eventEdit.link }

    column {
        card {
            messageBox(model.messageFlow)
            textBlock("Does the event have a website? We can try to read the details directly from the content. " +
                    "Then you can edit the details below.")
            row {
                textField("website", modify(Flex1), model::setEventLink, linkFlow)
                button("🤖 read website", onClick = model::readEventWebsite)
            }
        }

        card {
            // viewEventEditor(model.stateNow.eventEdit, model.app, editFlow.filterNotNull(), model::setEventEdit)
            row(modify(JustifyContentSpaceBetween)) {
                button("start over", modify(Secondary), model::reset)
                row {
                    messageBox(model.validEventFlow)
                    button("create", onClick = model::postEvent)
                }
            }
        }
    }
}

fun ViewContext<EventScoutProto>.reviewLocationPanel() {
    val editFlow = model.stateFlow.mapDistinct { it.locationEdit }.filterNotNull()
    val locationEdit = model.stateNow.locationEdit ?: error("location not found")

    column {
        card {
            messageBox(model.messageFlow)
            textBlock("Since this is a new location on Streetlight, let's provide some information about it. We can reuse it for other events.")
            textBlock("You can start by providing a website and we'll find out as much as we can. " +
                    "Then you can edit the details below and fill in any missing information.")
            row {
                textField("website", modify(Flex1), model::setWebsite, model.stateFlow.mapDistinct { it.website })
                button("🤖 read website", onClick = model::readLocationWebsite)
            }
        }

        card {
            // viewLocationEditor(locationEdit, model.app, editFlow, false, model::setEdit)
            row(modify(JustifyContentSpaceBetween)) {
                button("start over", modify(Secondary), onClick = model::reset)
                button("create location", modify(Accent), onClick = model::postLocation)
            }
        }
    }
}

fun ViewContext<EventScoutProto>.findLocationPanel() {
    val queryFlow = model.stateFlow.mapDistinct { it.query }
    val locationsFlow = model.stateFlow.mapDistinct { it.locations }

    card {
        messageBox(model.messageFlow)
        row(modify(JustifyContentEnd, AlignItemsCenter)) {
            textBlock("Move the map target to the location.", modify(Dim))
            button("Here", onClick = model::here)
        }

        filigree {
            heading3("Search")
        }

        textBlock("We can search for the location's name, address, city, etc.", modify(Dim, TextAlignCenter))

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