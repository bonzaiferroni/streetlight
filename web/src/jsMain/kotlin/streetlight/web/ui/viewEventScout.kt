package streetlight.web.ui

import koala.css.AlignItemsEnd
import koala.css.AlignItemsStart
import koala.css.Dim
import koala.css.Flex1
import koala.css.JustifyEnd
import koala.css.Width100
import koala.css.modify
import koala.dom.RenderContext
import koala.dom.ViewContext
import koala.dom.box
import koala.dom.button
import koala.dom.card
import koala.dom.column
import koala.dom.itemsBlock
import koala.dom.messageBox
import koala.dom.row
import koala.dom.switch
import koala.dom.textBlock
import koala.dom.textField
import koala.dom.viewGeoMap
import koala.dom.viewOf
import koala.html.heading4
import koala.html.spacer
import koala.model.mapDistinct
import streetlight.model.data.Galaxy
import streetlight.web.EventScoutRoute
import streetlight.web.model.EventScout
import streetlight.web.model.Streetlight
import streetlight.web.shells.cardOf

fun RenderContext.viewEventScout(app: Streetlight, galaxy: Galaxy) {
    val model = EventScout(app, renderScope)

    column {
        viewGeoMap(app.geoMap, app.appScope)

        viewOf(model) {
            pointFinderPanel()
        }
    }
}

fun ViewContext<Streetlight>.viewEventScoutRoute() {
    routeBlock<EventScoutRoute, Galaxy>({
        api.readGalaxy(it.pathId)
    }) { galaxy ->
        viewEventScout(model, galaxy)
    }
}

fun ViewContext<EventScout>.pointFinderPanel() {
    val queryFlow = model.stateFlow.mapDistinct { it.query }
    val locationsFlow = model.stateFlow.mapDistinct { it.locations }

    card {
        messageBox(model.messageFlow)
        row(modify(JustifyEnd)) {
            textBlock("Move the map target to the location.", modify(Dim))
            button("Here", onClick = model::here)
        }

        spacer("or")

        card {
            heading4("Search")
            textBlock("We can search for the location's name, address, city, etc.", modify(Dim))

            row(modify(AlignItemsStart)) {
                column(modify(Flex1, AlignItemsEnd)) {
                    textField("search", modify(Width100), model::setQuery, queryFlow, placeholder = "Search by name or address")
                }
                button("Search", onClick = model::searchQuery)
            }

            itemsBlock(locationsFlow) { location ->
                box {
                    cardOf(location)
                }
            }
        }
    }
}