package streetlight.web.ui

import kampfire.model.handleResponse
import koala.css.AlignItemsCenter
import koala.css.Magic
import koala.css.Blur
import koala.css.Class
import koala.css.SlideLeft
import koala.css.modify
import koala.dom.*
import koala.html.GeoMapKey
import koala.html.Id
import koala.html.heading2
import koala.html.textLabel
import koala.model.Portal
import kotlinx.coroutines.launch
import kotlinx.html.js.div
import streetlight.web.HomeRoute
import streetlight.web.io.ApiClient
import streetlight.web.model.DataCache
import streetlight.web.model.StreetMap
import streetlight.web.model.Streetlight

fun RenderContext.wireStreetMap() {
    val streetMap = app.get<StreetMap>()
    val cache = app.get<DataCache>()

    renderScope.launch {
        portal.routeFlowOf<HomeRoute>().collect {
            val galaxyIds = cache.topGalaxies.getItems().map { it.galaxyId }
            // td: gather initial posts from json in html
            val posts = api.readPosts(galaxyIds).handleResponse(toaster::toast) ?: return@collect
            streetMap.setPosts(posts)
        }
    }

    // wireMapPanel()
}

fun RenderContext.wireMapPanel() {
    wireBlock(GeoMapKey.Panel) {
        tabs(Id("map-panel-tabs")) {
            tab("Posts") {
                column {
                    textBlock("yer posts")
                }
            }
            tab("Controls") {
                column {
                    viewMapControls()
                }
            }
        }
    }
}

private val modifyCardItems = modify(Magic, Blur, SlideLeft)

fun RenderContext.mapPanelCard(label: String, block: RenderContext.() -> Unit) {
    card(modify(MapPanel.card)) {
        // header
        container(modify(MapPanel.grid, AlignItemsCenter)) {
            heading2(label, modify(MapPanel.cardHeading))
            div { }
            textLabel("visibility")
            textLabel("starts")
        }
        block()
    }
}

object MapPanel {
    val container = Class("map-event-panel")
    val card = Class("map-panel-card")
    val cardHeading = Class("map-panel-card-heading")
    val grid = Class("map-panel-card-grid")
}