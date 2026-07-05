package streetlight.web.ui

import kampfire.model.handleOutcome
import koala.SvgFile
import koala.css.*
import koala.dom.*
import koala.html.heading3
import koala.html.logo
import kotlinx.browser.document
import kotlinx.coroutines.delay
import streetlight.web.CityMap
import streetlight.web.CityMapRoute
import streetlight.web.EarthRoute
import streetlight.web.GalaxyMapRoute
import streetlight.web.HomeRoute
import streetlight.web.model.Earth
import streetlight.web.GalaxyMap
import streetlight.web.pages.AppBodyKey

fun AppScope.viewEarth(model: Earth) {
    box(EarthStyle.Id, modify(Size100P)) {
        val cameraController = geoMapMount(mod = modify(EarthStyle.Map))
        column(modify(Gap0, PointerEventsNone)) {
            // earthHeader(model)
            div(modify(EarthStyle.Grid, Padding1, Flex1, MinHeight0)) {
                earthUnboundedOverlay(model, cameraController)
                earthChrome(model)
                // earthList(model)
                earthFocus(model)
            }.flowModifier(model.isFocusedFlow, EarthStyle.IsFocused, parentScope)
        }
    }.flowModifier(model.isMovingFlow, EarthStyle.IsMoving, parentScope)
}

fun AppScope.viewEarthRoute() {
    var isVisible = false
    val element = document.getElementById(AppBodyKey.FullScreenId)

    launchEffect {
        portal.routeFlow.collect { route ->
            when (route) {
                is EarthRoute -> {
                    if (!isVisible) {
                        val map = when (route) {
                            is GalaxyMapRoute -> route.slug?.let { slug ->
                                api.readGalaxy(slug).handleOutcome(toaster::toast)?.let { GalaxyMap(it) }
                            } ?: GalaxyMap(null)
                            is CityMapRoute -> route.slug?.let { slug ->
                                api.readCity(slug).handleOutcome(toaster::toast)?.let { CityMap(it) }
                            } ?: CityMap(null)
                        }
                        element.replaceRender(app, parentScope) {
                            val model = app.getEarthMap(parentScope, map)
                            viewEarth(model)
                        }
                        element.modify(Reveal)
                        isVisible = true
                    }
                }
                else -> {
                    if (isVisible) {
                        element.unmodify(Reveal)
                        isVisible = false
                        delay(KoalaTheme.MAGIC_INTERVAL.toLong())
                        clearRender(element)
                    }
                }
            }
        }
    }
}

fun AppScope.earthHeader(model: Earth) {
    val iconMod = modify(Width5, Aspect1)
    row(modify(EarthStyle.Header, AlignItemsCenter, PaperGradientBg, Padding1, PointerEventsAuto, BlurBackdrop)) {
        flowBlock(model.mapFlow, modify(Flex1)) { map ->
            when (map) {
                null -> row {
                    icon(SvgFile.Helm, iconMod)
                    logo()
                }
                else -> row(modify(AlignItemsCenter)) {
                    icon(SvgFile.ArrowLeft, iconMod).onClick {
                        portal.go(GalaxyMapRoute(null))
                    }
                    heading3(map.title, modify(LineHeight115, SingleLine, Bold))
                }
            }
        }
        icon(SvgFile.GearLarge, iconMod).onClick { portal.go(HomeRoute) }
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