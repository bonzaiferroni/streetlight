package koala.model

import kampfire.model.GeoRect
import koala.dom.onEvent
import koala.external.maplibregl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import web.html.HTMLElement
import web.pointer.CLICK
import web.pointer.PointerEvent
import kotlin.time.Duration.Companion.milliseconds

/**
 * Draws the layers of the [MarkerMap] the [geoMap] shows and keeps its focused marker marked, clearing the focus on a
 * click of the map itself. A new marker map clears the stage before its layers are drawn.
 */
class GeoRender(
    private val windowElement: HTMLElement,
    private val widget: maplibregl.Map,
    private val lightLayer: LightLayer,
    private val camera: GeoCamera,
    private val scope: CoroutineScope,
    private val geoMap: GeoMap,
) {
    private var layerRenders: List<GeoLayerAdapter> = emptyList()
    private var focusRender: PointMarkerElement? = null

    init {
        val element = windowElement.querySelector(".maplibregl-canvas") ?: error("canvas not found")
        element.onEvent(PointerEvent.CLICK) {
            geoMap.markerMapState.now?.setFocus(null)
        }

        scope.launch {
            while (!widget.loaded()) {
                delay(10.milliseconds)
            }

            launch {
                geoMap.markerMapState.flow.collectLatest { markerMap ->
                    clear()
                    if (markerMap == null) return@collectLatest
                    coroutineScope {
                        launch {
                            markerMap.layersState.flow.collect { layers -> syncLayers(markerMap, layers) }
                        }
                        launch {
                            markerMap.focusState.flow.collect(::renderFocus)
                        }
                    }
                }
            }

            launch {
                camera.stateFlow.collect {
                    setBounds(it.view, it.zoom, it.isMoving)
                }
            }
        }
    }

    /** Draws each of the [layers] of [markerMap], keeping the drawings of those already drawn. */
    private fun syncLayers(markerMap: MarkerMap, layers: List<GeoLayer>) {
        layerRenders.forEach { render ->
            if (layers.any { it.layerId == render.layer.layerId }) return@forEach
            render.dispose()
        }
        layerRenders = layers.map { layer ->
            layerRenders.firstOrNull { it.layer.layerId == layer.layerId }
                ?: GeoLayerAdapter(
                    layer = layer,
                    widget = widget,
                    lightLayer = lightLayer,
                    scope = CoroutineScope(scope.coroutineContext + SupervisorJob()),
                    markerMap = markerMap,
                    onPointsSet = { renderFocus(markerMap.focusState.now) },
                ).also { render ->
                    val state = camera.stateNow
                    with(widget) { render.setBounds(state.view.scaleBy(BoundsScale), state.zoom, state.isMoving) }
                }
        }
    }

    /** Disposes the drawing of every layer. */
    private fun clear() {
        layerRenders.forEach { it.dispose() }
        layerRenders = emptyList()
        focusRender = null
    }

    /** Marks the drawn marker of [focus], or none when it is `null` or not drawn. */
    private fun renderFocus(focus: PointMarker?) {
        val render = focus?.markerId?.let { markerId ->
            layerRenders.firstNotNullOfOrNull { render ->
                render.pointElements[markerId]
            }
        }
        if (focusRender == render) return
        focusRender?.unfocus()
        render?.focus()
        focusRender = render
    }

    /** Culls each layer's markers to [bounds], widened a little, and reclusters on a change of zoom. */
    fun setBounds(bounds: GeoRect, zoom: Float, isMoving: Boolean) {
        // resize to ensure comprehensive view region
        val bounds = bounds.scaleBy(BoundsScale)
        // set marker visibility
        layerRenders.forEach { layer ->
            with(widget) {
                layer.setBounds(bounds, zoom, isMoving)
            }
        }
    }
}

/** The scale of the camera's view within which markers are kept. */
private const val BoundsScale = 1.2f
