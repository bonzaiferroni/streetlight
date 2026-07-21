package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.AppRoute
import koala.html.btn
import koala.html.featureImage
import koala.html.filigree
import koala.html.heading3
import koala.html.heading4
import koala.html.navigationIfNotNull
import koala.model.ClusterFocus
import koala.model.FeatureMarker
import koala.model.MarkerFocus
import koala.model.PointMarker
import kotlinx.html.FlowContent
import kotlinx.html.hr
import streetlight.model.data.Entity
import streetlight.model.ui.CityRoute
import streetlight.model.ui.GalaxyRoute
import streetlight.web.layouts.ColorScheme
import streetlight.web.layouts.cellBlock
import streetlight.web.layouts.cellContentOf
import streetlight.web.layouts.eventRoute
import streetlight.web.layouts.locationRoute
import streetlight.web.layouts.route
import streetlight.web.model.CityMarker
import streetlight.web.model.Earth
import streetlight.web.model.EventMarker
import streetlight.web.model.GalaxyMarker
import streetlight.web.model.LocationMarker

fun ViewScope.earthFocus(model: Earth) {
    flowBlock(null, model.focusFlow, modify(EarthStyle.Focus, Magic)) { focus ->
        when (focus) {
            is ClusterFocus -> tabs(
                mod = modify(PointerEventsAuto, Height100P),
                viewportMod = modify(Flex1, OverflowYAuto, BorderRadius2)
            ) {
                focus.members.forEach { marker ->
                    val marker = marker as? FeatureMarker ?: return@forEach
                    tab(marker.label ?: marker.typeLabel ?: "thing", marker.colorScheme) {
                        markerPanel(marker)
                    }
                }
            }

            is MarkerFocus -> div(modify(Height100P, OverflowYAuto, BorderRadius2)) {
                markerPanel(focus.marker)
            }
            null -> return@flowBlock
        }
    }
}

private fun ViewScope.markerPanel(marker: PointMarker) {
    when (marker) {
        is EventMarker -> focusPanel(
            post = marker.event,
            route = marker.event.eventRoute,
            subroute = marker.event.locationRoute,
            colorScheme = ColorScheme.Accent,
            cells = cellContentOf(marker.event, false)
        )
        is LocationMarker -> focusPanel(
            post = marker.location,
            route = marker.location.route,
            colorScheme = ColorScheme.Primary,
            cells = cellContentOf(marker.location)
        )
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
    route: AppRoute,
    subroute: AppRoute? = null,
    colorScheme: ColorScheme = ColorScheme.Primary,
    cells: (FlowContent.() -> Unit)? = null,
) {
    val extraLinks = post.links
    card(modify(Gap0, Padding0, BlurBackdrop, PointerEventsAuto, BorderSolid2Px, EarthStyle.MoveDimmer)) {
        setStyle(Property.ColorScheme.to(colorScheme.cssValue))
        column(modify(Gap0)) {
            featureImage(post.image, modify(Flex1))
            cells?.let {
                cellBlock(modify(FlexWrap), cells)
            }
        }
        column(modify(Padding1)) {
            column(modify(Gap0, TextAlignCenter)) {
                navigation(route) {
                    heading3(post.label, modify(Bold))
                }
                post.sublabel?.let {
                    navigationIfNotNull(subroute) {
                        heading4(it, modify(OpacityHigh))
                    }
                }
            }
            if (extraLinks != null) {
                filigree {
                    row {
                        extraLinks.forEach {
                            btn(it.label, it.url, modify(Zen))
                        }
                    }
                }
            } else {
                hr { }
            }
            post.body?.let {
                markdown(it, modify(Padding1))
            }
        }
    }
}