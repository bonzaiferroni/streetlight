package koala.model

import kampfire.model.GeoBounds
import koala.dom.onClick
import koala.external.maplibregl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import web.html.HTMLElement
import kotlin.time.Duration.Companion.milliseconds

class GeoRender(
    private val windowElement: HTMLElement,
    private val jsMap: maplibregl.Map,
    private val camera: GeoCamera,
    private val scope: CoroutineScope,
    private val geoMap: GeoMap,
) {
    private var layerRenders: List<GeoLayerRender> = emptyList()
    private var focusRender: PointRender? = null

    init {
        val element = windowElement.querySelector(".maplibregl-canvas") ?: error("canvas not found")
        element.onClick {
            setFocus(null)
        }

        scope.launch {
            while (!jsMap.loaded()) {
                delay(10.milliseconds)
            }

            launch {
                // sync layers
                geoMap.layersFlow.collect { layers ->
                    layerRenders.forEach { view ->
                        if (layers.any { it.layerId == view.layer.layerId }) return@forEach
                        view.dispose()
                    }
                    layerRenders = layers.map { layer ->
                        layerRenders.firstOrNull { it.layer.layerId == layer.layerId }
                            ?: GeoLayerRender(
                                layer = layer,
                                jsMap = jsMap,
                                scope = CoroutineScope(scope.coroutineContext + SupervisorJob()),
                                onFocus = ::setFocus
                            )
                    }
                }
            }

            launch {
                geoMap.focusFlow.collect(::setFocus)
            }

            launch {
                camera.stateFlow.collect {
                    setBounds(it.bounds, it.zoom, it.isMoving)
                }
            }
        }
    }

    fun setFocus(focus: GeoFocus?) {
        val markerId = when (focus) {
            is MarkerFocus -> focus.marker.markerId
            is ClusterFocus -> focus.principal.markerId
            else -> null
        }

        val render = markerId?.let { markerId ->
            layerRenders.firstNotNullOfOrNull { render ->
                render.pointRenders[markerId]
            }
        }

        setFocusRender(render)
        geoMap.setFocus(focus)
    }

    internal fun setFocusRender(render: PointRender?) {
        if (focusRender == render) return
        focusRender?.unfocus()

        render?.focus()
        focusRender = render

    }

    fun setBounds(bounds: GeoBounds, zoom: Float, isMoving: Boolean) {
        // resize to ensure comprehensive view region
        val bounds = bounds.resizeBy(1.2f)
        // set marker visibility
        layerRenders.forEach { layer ->
            layer.setBounds(bounds, zoom, isMoving)
        }
    }
}