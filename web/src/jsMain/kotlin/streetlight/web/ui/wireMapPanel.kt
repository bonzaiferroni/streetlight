package streetlight.web.ui

import kampfire.model.Ok
import kampfire.model.Problem
import koala.css.AlignItemsCenter
import koala.css.Magic
import koala.css.Blur
import koala.css.Class
import koala.css.SlideLeft
import koala.css.modify
import koala.dom.*
import koala.html.GeoMapSelector
import koala.html.Id
import koala.html.heading2
import koala.html.textLabel
import kotlinx.coroutines.launch
import kotlinx.html.js.div
import streetlight.web.HomeRoute
import streetlight.web.io.getDataOrNull
import streetlight.web.model.Streetlight

fun ViewContext<Streetlight>.wireStreetMap() {
    val streetMap = model.streetMap

    renderScope.launch {
        portal.routeFlowOf<HomeRoute>().collect {
            val galaxyIds = userCache.topGalaxies.getItems().map { it.galaxyId }
            // td: gather initial posts from json in html
            val posts = api.readPosts(galaxyIds).getDataOrNull() ?: return@collect
            streetMap.setPosts(posts)
        }
    }

    // wireMapPanel()
}

fun ViewContext<Streetlight>.wireMapPanel() {
    wireBlock(GeoMapSelector.panel) {
        tabs(Id("map-panel-tabs")) {
            tab("Posts") {
                column {
                    textBlock("yer posts")
                }
            }
            tab("Controls") {
                column {
                    viewMapControls(model)
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