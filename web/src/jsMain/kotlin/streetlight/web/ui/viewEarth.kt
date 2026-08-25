package streetlight.web.ui

import kampfire.model.toDataOrNull
import koala.css.*
import koala.css.Padding1
import koala.dom.*
import koala.html.filigree
import koala.html.heading3
import kotlinx.browser.document
import streetlight.model.ui.CityMap
import streetlight.model.ui.CityMapRoute
import streetlight.model.ui.EarthRoute
import streetlight.model.ui.GalaxyMapRoute
import streetlight.web.model.Earth
import streetlight.model.ui.GalaxyMap
import streetlight.web.pages.AppBody

fun ViewScope.viewEarth(model: Earth) {
    box(EarthStyle.Container, modify(Size100P)) {
        val cameraController = geoMapMount(mod = modify(EarthStyle.Map))
        column(modify(Gap0, PointerEventsNone)) {
            div(modify(EarthStyle.Grid, Padding1, Flex1, MinHeight0)) {
                earthUnboundedOverlay(model, cameraController)
                earthHeader(model)
                earthMenu(model)
                // earthList(model)
                earthFocus(model)
            }.flowModifier(model.isFocusedField, EarthStyle.IsFocused, contentScope)
        }
    }.flowModifier(model.isMovingField, EarthStyle.IsMoving, contentScope)
}

fun ViewScope.viewEarthRoute() {
    var isVisible = false
    val element = document.getElementById(AppBody.FullScreen)

    launchEffect(ViewScope::viewEarthRoute) {
        portal.routeFlow.collect { route ->
            when (route) {
                is EarthRoute -> {
                    if (!isVisible) {
                        val map = when (route) {
                            is GalaxyMapRoute -> route.slug?.let { slug ->
                                api.readGalaxy(slug).toDataOrNull(toaster)?.let { GalaxyMap(it) }
                            } ?: GalaxyMap(null)
                            is CityMapRoute -> route.slug?.let { slug ->
                                api.readCity(slug).toDataOrNull(toaster)?.let { CityMap(it) }
                            } ?: CityMap(null)
                        }
                        this@viewEarthRoute.mountChildView("earth", element) {
                            val model = app.getEarthMap(contentScope, map)
                            viewEarth(model)
                        }
                        element.modify(Reveal)
                        isVisible = true
                    }
                }
            }
        }
    }
    onDispose {
        element.unmodify(Reveal)
    }
}

fun ViewScope.earthHeader(model: Earth) {
    flowBlock(model.mapField, modify(EarthStyle.Header, EarthStyle.MoveDimmer, Magic)) { map ->
        column(modify(AlignItemsCenter)) {
            filigree(modify(AlignSelfStretch, EarthStyle.MapTitle)) {
                heading3(map.title)
            }
            // button("Show All", modify(Zen, PointerEventsAuto, BlurBackdrop)).onClick(model::showAll)
        }
    }
}



// fun TagScope.markerItem(
//    thumb: Url?,
//    label: String,
//    sublabel: String?,
//    onClick: () -> Unit
//) {
//    row(modify(Height8, BorderRadius2, OverflowClip, Gap0, WidthFitContent, PointerEventsAuto)) {
//        image(thumb, modify(Aspect1))
//        column(modify(EarthStyle.ListDetail, PaperGradientBg, Padding1, Gap0)) {
//            heading3(label, modify(Bold, LineHeight115, SingleLine))
//            sublabel?.let {
//                textBlock(sublabel, modify(SmallText))
//            }
//        }
//    }.onClick(onClick)
//}

//fun AppScope.earthList(model: EarthMap) {
//    val reversedItems = model.boundedMarkersFlow.map { it.reversed() } // reverse shows new items on top
//    box(modify(Earth.List, Earth.MoveDimmer)) {
//        itemsBlock(
//            flow = reversedItems,
//            mod = modify(Magic, SlideLeft),
//        ) { marker ->
//            when (marker) {
//                is GalaxyMarker -> {
//                    val galaxy = marker.galaxy
//                    // if (galaxy.eventCount + galaxy.locationCount == 0) return@itemsBlock
//                    markerItem(galaxy.images.thumb, galaxy.name, buildString {
//                        if (galaxy.eventCount > 0) append("${galaxy.eventCount} events")
//                        if (galaxy.locationCount > 0) {
//                            if (isNotEmpty()) append(" • ")
//                            append("${galaxy.locationCount} locations")
//                        }
//                    }) {
//                        portal.go(EarthRoute(marker.galaxy.slug))
//                    }
//                }
//                is EventMarker -> {
//                    markerItem(marker.post.images.thumb, marker.post.label, marker.post.sublabel) {
//                        model.setFocus(marker)
//                    }
//                }
//                is LocationMarker -> {
//                    markerItem(marker.location.images.thumb, marker.location.label, marker.location.sublabel) { }
//                }
//            }
//        }
//    }
//}