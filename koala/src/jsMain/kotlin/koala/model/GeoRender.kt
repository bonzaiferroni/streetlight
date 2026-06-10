package koala.model

import kampfire.model.GeoBounds
import kampfire.model.GeoPoint
import koala.external.maplibregl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.w3c.dom.HTMLElement

class GeoRender(
    private val windowElement: HTMLElement,
    private val jsMap: maplibregl.Map,
    private val camera: GeoCamera,
    private val scope: CoroutineScope,
    private val geoMap: GeoMap,
) {
    private var layerRenders: List<GeoLayerRender> = emptyList()
    private var focus: PointRender? = null

    init {
        val element = windowElement.querySelector(".maplibregl-canvas") ?: error("canvas not found")
        element.addEventListener("click", {
            setFocus(null)
        })

        scope.launch {
            while (!jsMap.loaded()) {
                delay(10)
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

    fun setFocus(marker: PointMarker?) {
        if (focus?.marker == marker) return
        focus?.unfocus()

        val render = marker?.let { entity ->
            layerRenders.firstNotNullOfOrNull { render ->
                render.pointRenders[entity.markerId]
            }
        }
        render?.focus()
        focus = render
        geoMap.setFocus(marker)
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