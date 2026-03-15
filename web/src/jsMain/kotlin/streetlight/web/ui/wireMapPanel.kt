package streetlight.web.ui

import koala.css.AlignItemsCenter
import koala.css.Magic
import koala.css.Blur
import koala.css.Css
import koala.css.CircleShape
import koala.css.Height3
import koala.css.SlideLeft
import koala.css.Width100
import koala.css.modify
import koala.dom.*
import koala.html.GeoMapSelector
import koala.html.Id
import koala.html.heading2
import koala.html.image
import koala.html.label
import koala.model.mapDistinct
import kotlinx.coroutines.launch
import kotlinx.html.js.div
import streetlight.web.HomeRoute
import streetlight.web.model.Streetlight

fun ViewContext<Streetlight>.wireStreetMap() {
    val streetMap = model.streetMap

    renderScope.launch {
        portal.routeFlowOf<HomeRoute>().collect {
            val galaxyIds = userCache.galaxy.getItems().map { it.galaxyId }
            // td: gather initial posts from json in html
            val posts = api.readPosts(galaxyIds) ?: return@collect
            streetMap.setPosts(posts)
        }
    }

    wireMapPanel()
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
            label("visibility")
            label("starts")
        }
        block()
    }
}

object MapPanel {
    val container = Css("map-event-panel")
    val card = Css("map-panel-card")
    val cardHeading = Css("map-panel-card-heading")
    val grid = Css("map-panel-card-grid")
}