package streetlight.web.ui

import kampfire.model.Url
import kampfire.model.thumb
import koala.SvgFile
import koala.css.*
import koala.dom.*
import koala.html.heading3
import koala.html.image
import koala.html.logo
import koala.html.spacer
import kotlinx.browser.document
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import streetlight.web.EarthRoute
import streetlight.web.HomeRoute
import streetlight.web.model.EarthMap
import streetlight.web.model.EventMarker
import streetlight.web.model.GalaxyMarker
import streetlight.web.model.LocationMarker
import streetlight.web.pages.AppBodyKey

fun RenderContext.viewEarthMap(model: EarthMap) {
    div(Earth.Id, modify(Size100P)) {
        val mount = geoMapMount(geoMap, appScope, mod = modify(Earth.Map))
        earthHeader(model)
        earthUnboundedOverlay(model, mount)
        earthWindow(model)
        earthPanel(model)
    }.flowModifier(model.isMovingFlow, Earth.IsMoving, renderScope)
}

fun RenderContext.viewEarthMapRoute() {
    var isVisible = false
    val element = document.getElementById(AppBodyKey.FullScreenId)

    renderScope.launch {
        portal.routeFlow.collect { route ->
            when (route) {
                is EarthRoute -> {
                    if (!isVisible) {
                        replaceRender(element) {
                            val model = app.getEarthMap(renderScope)
                            viewEarthMap(model)
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

fun RenderContext.earthHeader(model: EarthMap) {
    val iconMod = modify(Width5, Aspect1, ZIndex2)
    row(modify(Earth.Header, AlignItemsCenter, PaperGradientBg, Padding1, ZIndex2)) {
        flowBlock(model.galaxyFlow, modify(Flex1)) { galaxy ->
            when (galaxy) {
                null -> row {
                    icon(SvgFile.Helm, iconMod)
                    logo()
                }
                else -> row(modify(AlignItemsCenter)) {
                    icon(SvgFile.ArrowLeft, iconMod).onClick {
                        portal.go(EarthRoute(null))
                    }
                    heading3(galaxy.name, modify(LineHeight115, SingleLine, Bold))
                }
            }
        }
        icon(SvgFile.Settings, iconMod).onClick { portal.go(HomeRoute) }
    }
}

fun RenderContext.earthWindow(model: EarthMap) {
    column(modify(Earth.Window, Earth.MoveDimmer, ZIndex2, PointerEventsNone, PaddingLeft1)) {
        spacer(modify(Flex1))
        textBlock(model.summaryFlow.map { map -> map?.entries?.joinToString(" • ") { "${it.key.label}: ${it.value.size}" } })
    }
}

fun RenderContext.earthPanel(model: EarthMap) {
    val reversedItems = model.boundedMarkersFlow.map { it.reversed() } // reverse shows new items on top
    itemsBlock(
        flow = reversedItems,
        mod = modify(Earth.Panel, Earth.MoveDimmer, ZIndex2, Magic, SlideLeft, Margin1, PointerEventsNone),
    ) { marker ->
        when (marker) {
            is GalaxyMarker -> {
                val galaxy = marker.galaxy
                if (galaxy.eventCount + galaxy.locationCount == 0) return@itemsBlock
                markerItem(galaxy.images.thumb, galaxy.name, buildString {
                    if (galaxy.eventCount > 0) append("${galaxy.eventCount} events")
                    if (galaxy.locationCount > 0) {
                        if (isNotEmpty()) append(" • ")
                        append("${galaxy.locationCount} locations")
                    }
                }) {
                    portal.go(EarthRoute(marker.galaxy.slug))
                }
            }
            is EventMarker -> {
                markerItem(marker.post.images.thumb, marker.post.label, marker.post.sublabel) { }
            }
            is LocationMarker -> {
                markerItem(marker.location.images.thumb, marker.location.label, marker.location.sublabel) { }
            }
        }
    }
}

fun DOMContext.markerItem(
    thumb: Url?,
    label: String,
    sublabel: String?,
    onClick: () -> Unit
) {
    row(modify(Height8, BorderRadius2, OverflowClip, Gap0, WidthFitContent, PointerEventsAuto)) {
        image(thumb, modify(Aspect1))
        column(modify(PaperGradientBg, Padding1, Gap0)) {
            heading3(label, modify(Bold, LineHeight115, SingleLine))
            sublabel?.let {
                textBlock(sublabel, modify(SmallText))
            }
        }
    }.onClick(onClick)
}

