package koala.model

import kampfire.model.storeOf
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/** A layer of markers on the map, with its points, lines and visibility. */
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

    fun setLines(value: List<LineMarker>) = state.update { it.copy(lines = value) }
    fun setPoints(value: List<PointMarker>) = state.update { it.copy(points = value) }
    fun setIsVisible(value: Boolean) = state.update { it.copy(isVisible = value) }
}

/** The markers of a [GeoLayer] and whether it is visible. */
data class GeoLayerState(
    val isVisible: Boolean = true,
    val points: List<PointMarker> = emptyList(),
    val lines: List<LineMarker> = emptyList(),
)

/** The id of a [GeoLayer]. */
@JvmInline
@Serializable
value class GeoLayerId(val string: String) {
    override fun toString() = string
}

/** The settings of a [GeoLayer]. With [clusterRadiusPx], points closer than that on screen are clustered. */
data class GeoLayerConfig(val layerId: GeoLayerId, val clusterRadiusPx: Int? = null)