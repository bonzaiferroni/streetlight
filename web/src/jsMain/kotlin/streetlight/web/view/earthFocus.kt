package streetlight.web.view

import koala.modifier.*
import koala.dom.*
import koala.model.WebRoute
import koala.component.btn
import koala.component.containImage
import koala.component.filigree
import koala.component.heading3
import koala.component.heading4
import koala.component.navigationIfNotNull
import koala.model.PointMarker
import kotlinx.css.pct
import kotlinx.html.hr
import streetlight.model.data.Entity
import streetlight.model.ui.CityRoute
import streetlight.model.ui.GalaxyRoute
import streetlight.web.model.ThemeColor
import streetlight.web.layout.FeedRowStyle
import streetlight.web.layout.FeedSection
import streetlight.web.layout.configureFeedRow
import streetlight.web.layout.cellGrid
import streetlight.web.model.toCells
import streetlight.web.model.toButtons
import streetlight.web.model.CityMarker
import streetlight.web.model.Earth
import streetlight.web.model.EventMarker
import streetlight.web.model.GalaxyMarker
import streetlight.web.model.InflateMarker
import streetlight.web.model.LocationMarker
import koala.dom.component.*
import koala.dom.layout.*
import koala.dom.markdown.*
import streetlight.web.dom.layout.feedRow

context(model: Earth)
fun ViewScope.earthFocus() {
    flowBlock(model.focusEntitiesState,
        modify(EarthStyle.Focus, MoonShadow, OverflowYAuto, MaxHeight(100.pct), PointerEventsAuto, ZenBg, BlurBackdrop)
    ) { entitiesOrNull ->
        val entities = entitiesOrNull ?: return@flowBlock
        column(modify(FeedSection.FeedColumnMod, FadeIn)) {
            entities.forEachIndexed { index, entity ->
                when (index) {
                    0 -> div(FeedRowStyle.Featured) { configureFeedRow(entity) }
                    else -> feedRow(entity, mod = Padding(1))
                }
            }
        }
    }
//    flowBlock(model.isInflatingState, EarthStyle.Focus) { isInflating ->
//        when (isInflating) {
//            true -> textBlock("loading")
//            else -> {
//
//            }
//        }
//    }
}

private fun ViewScope.markerRow(marker: PointMarker) {
    when (marker) {
        is EventMarker -> feedRow(marker.event)
        is LocationMarker -> feedRow(marker.location)
        is CityMarker -> focusPanel(
            post = marker.city,
            route = CityRoute(marker.city.slug),
        )
        is GalaxyMarker -> focusPanel(
            post = marker.galaxy,
            route = GalaxyRoute(marker.galaxy.slug)
        )
    }
}

private fun ViewScope.focusPanel(
    post: Entity,
    route: WebRoute,
    subroute: WebRoute? = null,
    colorScheme: ThemeColor = ThemeColor.Primary,
) {
    val extraLinks = post.links
    card(modify(Gap0, Padding(0), BlurBackdrop, PointerEventsAuto, BorderSolid2Px, EarthStyle.MoveDimmer)) {
        setStyle(Css.ColorScheme.of(colorScheme.cssValue))
        column(Gap0) {
            containImage(post.image, Flex1)
            cellGrid(post.toCells(), post.toButtons())
        }
        column(Padding(1)) {
            column(modify(Gap0, TextAlignCenter)) {
                navigation(route) {
                    heading3(post.label, Bold)
                }
                post.sublabel?.let {
                    navigationIfNotNull(subroute) {
                        heading4(it, OpacityHigh)
                    }
                }
            }
            if (extraLinks != null) {
                filigree {
                    row {
                        extraLinks.forEach {
                            btn(it.label, it.url, Zen)
                        }
                    }
                }
            } else {
                hr { }
            }
            post.description?.let {
                markdown(it, Padding(1))
            }
        }
    }
}