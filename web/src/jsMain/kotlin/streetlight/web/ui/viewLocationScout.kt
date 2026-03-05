package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.heading3
import koala.html.propertyValue
import koala.model.mapDistinct
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.web.ReadEventRoute
import streetlight.web.model.*
import streetlight.web.shells.cardOf

fun RenderContext.viewLocationScout(app: AppContext) {
    val model = LocationScout(renderScope, app)
    column {
        viewGeoMap(app.geoMap, app.appScope)

        flowBlock(model.dataFlow, defaultMagic, magic = true) { data ->
            val point = data.point; val edit = data.edit; val location = data.location
            viewOf(model) {
                if (point == null) {
                    findPointStage()
                } else if (edit != null) {
                    if (location != null) {
                        finishedStage(location)
                    } else {
                        reviewStage(edit)
                    }
                } else {
                    creationStage()
                }
            }
        }
    }
}

fun ViewContext<LocationScout>.findPointStage() {
    card {
        column {
            messageBox(model.messageFlow, modify(Flex1))
            row {
                textField("search", modify(Flex1), placeholder = "Search by name or address")
                button("Search", onClick = model::searchOSM)
                button("Here", modify(Accent), onClick = model::here)
            }
        }
    }
}

fun ViewContext<LocationScout>.finishedStage(location: Location) {
    val portal = model.app.portal

    card {
        row {
            messageBox(model.messageFlow, modify(Flex1))
            button("Start over", onClick = model::reset)
            button("Post events", modify(Accent), onClick = {
                portal.go(ReadEventRoute(location))
            })
        }
    }
}

fun ViewContext<LocationScout>.reviewStage(edit: LocationEdit) {
    card {
        row {
            messageBox(model.messageFlow, modify(Flex1))
            button("Start over", onClick = model::reset)
            button("Needs edits")
            button("Looks good", modify(Accent), onClick = model::postLocation)
        }
        row(modify(AlignItemsStart)) {
            val imageUrl = edit.imageUrl
            if (imageUrl != null) {
                image(imageUrl, modify(Flex1, Width100))
            } else {
                box(modify(Flex1, CenterItems)) {
                    textBlock("no image")
                }
            }
            column(modify(Flex2)) {
                heading3(edit.name ?: "[No name found]")
                textBlock(edit.description ?: "[No description]")
                propertyValue("address", edit.address ?: "[No address]")
                propertyValue("link", edit.link ?: "[No link]")
                propertyValue("calendar", edit.eventsLink ?: "[No calendar]")
            }
        }
    }
}

fun ViewContext<LocationScout>.creationStage() {
    val locationsFlow = model.app.streetMap.locationsFlow

    column {
        card {
            messageBox(model.messageFlow)
            row {
                textField("link", modify(Flex1), model::setLink, model.stateFlow.mapDistinct { it.link })
                button("🤖 read link", onClick = model::readLink)
                button("📝 editor")
            }
        }

        itemsBlock(locationsFlow, defaultMagic, magic = true) { (location, events) ->
            box {
                cardOf(location)
            }
        }
    }
}