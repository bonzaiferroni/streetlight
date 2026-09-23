package streetlight.web.ui

import koala.modifier.*
import koala.dom.*
import koala.html.AppRoute
import koala.html.btn
import koala.html.featureImage
import koala.html.filigree
import koala.html.heading3
import koala.html.heading4
import koala.html.navigationIfNotNull
import koala.model.ClusterFocus
import koala.model.EntityMarker
import koala.model.MarkerFocus
import koala.model.PointMarker
import kotlinx.css.pct
import kotlinx.html.hr
import streetlight.model.data.Entity
import streetlight.model.ui.CityRoute
import streetlight.model.ui.GalaxyRoute
import streetlight.web.layouts.ThemeColor
import streetlight.web.layouts.FeedSection
import streetlight.web.layouts.cellGrid
import streetlight.web.layouts.cells
import streetlight.web.layouts.entityButtonsOf
import streetlight.web.model.CityMarker
import streetlight.web.model.Earth
import streetlight.web.model.EventMarker
import streetlight.web.model.GalaxyMarker
import streetlight.web.model.LocationMarker

fun ViewScope.earthFocus(model: Earth) {
    flowBlock(model.focusState,
        modify(EarthStyle.Focus, MoonShadow, OverflowYAuto, MaxHeight(100.pct), PointerEventsAuto, ZenBg, BlurBackdrop)
    ) { focus ->
        when (focus) {
            is ClusterFocus -> column(FeedSection.FeedColumnMod) {
                focus.members.forEach { marker ->
                    val marker = marker as? EntityMarker ?: return@forEach
                    markerRow(marker)
                }
            }

            is MarkerFocus -> markerRow(focus.marker)
            null -> return@flowBlock
        }
    }
}

private fun ViewScope.markerRow(marker: PointMarker) {
    when (marker) {
        is EventMarker -> feedRow(marker.event, true)
        is LocationMarker -> feedRow(marker.location, true)
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
    colorScheme: ThemeColor = ThemeColor.Primary,
) {
    val extraLinks = post.links
    card(modify(Gap0, Padding(0), BlurBackdrop, PointerEventsAuto, BorderSolid2Px, EarthStyle.MoveDimmer)) {
        setStyle(Css.ColorScheme.of(colorScheme.cssValue))
        column(Gap0) {
            featureImage(post.image, Flex1)
            cellGrid(post.cells, entityButtonsOf(post, false), FlexWrap)
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
            post.body?.let {
                markdown(it, Padding(1))
            }
        }
    }
}