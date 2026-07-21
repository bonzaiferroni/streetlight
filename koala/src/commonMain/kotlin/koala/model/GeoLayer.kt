package koala.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

class GeoLayer(
    val config: GeoLayerConfig,
) {
    val layerId get() = config.layerId

    private val state = storeOf(GeoLayerState())
    val stateFlow = state.flow
    val stateNow get() = state.now


    val pointsFlow = stateFlow.dedup { it.points }
    val linesFlow = stateFlow.dedup { it.lines }
    val isVisibleFlow = stateFlow.dedup { it.isVisible }

    fun setLines(value: List<LineMarker>) = state.setValue { it.copy(lines = value) }
    fun setPoints(value: List<PointMarker>) = state.setValue { it.copy(points = value) }
    fun setIsVisible(value: Boolean) = state.setValue { it.copy(isVisible = value) }
}

data class GeoLayerState(
    val isVisible: Boolean = true,
    val points: List<PointMarker> = emptyList(),
    val lines: List<LineMarker> = emptyList(),
)

@JvmInline
@Serializable
value class GeoLayerId(val string: String) {
    override fun toString() = string
}

data class GeoLayerConfig(val layerId: GeoLayerId, val clusterRadiusPx: Int? = null)