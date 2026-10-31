package streetlight.compose.charts

import androidx.compose.ui.graphics.Color

/** The configuration of a chart axis. */
interface AxisConfig {
    val tickCount: Int
    val color: Color?

    interface Side : AxisConfig {
        val side: AxisSide
    }

    sealed interface Bottom : AxisConfig
}

data class SideAxisAutoConfig(
    override val tickCount: Int,
    override val side: AxisSide,
    override val color: Color? = null,
) : AxisConfig.Side

data class BottomAxisAutoConfig(
    override val tickCount: Int,
    override val color: Color? = null,
) : AxisConfig.Bottom

data class BottomAxisConfig(
    val ticks: List<AxisTick>,
    override val color: Color? = null,
) : AxisConfig.Bottom {
    override val tickCount get() = ticks.size
}

data class AxisTick(
    val value: Double,
    val label: String,
)

enum class AxisSide {
    Left,
    Right,
}
