package koala.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

class GeoLayer(
    val layerId: GeoLayerId
) {
    private val state = storeOf(GeoLayerState())
    val stateFlow = state.flow
    val stateNow get() = state.now

    val pointsFlow = stateFlow.mapDistinct { it.points }
    val linesFlow = stateFlow.mapDistinct { it.lines }
    val isVisibleFlow = stateFlow.mapDistinct { it.isVisible }

    fun setLines(value: List<LineMarker>) = state.set { it.copy(lines = value) }
    fun setPoints(value: List<PointMarker>) = state.set { it.copy(points = value) }
    fun setIsVisible(value: Boolean) = state.set { it.copy(isVisible = value) }
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