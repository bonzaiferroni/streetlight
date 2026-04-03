package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.model.mapDistinct
import kotlinx.coroutines.flow.filterNotNull
import streetlight.web.model.LocationFinder
import streetlight.web.model.LocationFinderStage

fun ViewContext<LocationFinder>.viewLocationFinder() {
    val stageFlow = model.stateFlow.mapDistinct { it.stage }

    flowBlock(stageFlow, defaultMagic) { stage ->
        viewContextOf(model) {
            when (stage) {
                LocationFinderStage.Place -> viewLocationFinderPlace()
                LocationFinderStage.Edit -> viewLocationFinderEdit()
                LocationFinderStage.Complete -> viewLocationFinderComplete()
            }
        }
    }
}

fun ViewContext<LocationFinder>.viewLocationFinderPlace() {
    val app = model.app
    val queryFlow = model.stateFlow.mapDistinct { it.query }
    val locationsFlow = model.stateFlow.mapDistinct { it.locations }

    column {
        tabs {
            tab("Search") {
                textBlock("You can search by the location's name, address, city, etc.", modify(Dim))

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
            tab("Find on map") {
                column {
                    viewGeoMap(app.geoMap, app.appScope)
                    row(modify(JustifyContentEnd)) {
                        textBlock("Move the map target to the location.", modify(Dim))
                        button("Here", onClick = model::here)
                    }
                }
            }
        }
        messageBox(model.messageFlow)
    }
}

fun ViewContext<LocationFinder>.viewLocationFinderEdit() {
    val edit = model.stateNow.edit ?: error("edit not found")
    val editFlow = model.stateFlow.mapDistinct { it.edit }.filterNotNull()

    column {
        card {
            messageBox(model.messageFlow)
            row {
                textField("website", modify(Flex1), model::setWebsite, model.stateFlow.mapDistinct { it.website })
                button("🤖 read website", onClick = model::readLocationWebsite)
            }
        }

        card {
            viewLocationEditor(edit, model.app, editFlow, model::setEdit)
            row(modify(JustifyContentSpaceBetween)) {
                button("start over", modify(Secondary), onClick = model::reset)
                button("create location", modify(Accent), onClick = model::createLocation)
            }
        }
    }
}

fun ViewContext<LocationFinder>.viewLocationFinderComplete() {
    val location = model.stateNow.location ?: error("location not found")
    column {
        cardOf(location)
    }
}