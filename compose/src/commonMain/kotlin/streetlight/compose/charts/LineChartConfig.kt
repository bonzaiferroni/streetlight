package streetlight.compose.charts

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import kabinet.utils.toMetricString

@Stable
@Immutable
data class LineChartConfig<T>(
    val arrays: List<LineChartArray<T>>,
    override val contentColor: Color,
    override val isAnimated: Boolean = true,
    override val glowColor: Color? = null,
    override val bottomAxis: AxisConfig.Bottom? = null,
    override val startX: Double? = null,
    override val endX: Double? = null,
    override val provideLabelX: (Double) -> String = { it.toMetricString() },
    val onHoverPoint: ((HoverInfo<T>?) -> Unit)? = null,
    val provideX: ((T) -> Double)? = null,
) : ChartConfig

@Stable
@Immutable
data class LineChartArray<T>(
    val values: List<T>,
    val color: Color,
    val label: String? = null,
    val isBezier: Boolean = true,
    val axis: AxisConfig.Side? = null,
    val floor: Double? = null,
    val ceiling: Double? = null,
    val key: Any? = null,
    val provideLabelY: (Double) -> String = { it.toMetricString() },
    val provideY: (T) -> Double,
)

data class HoverInfo<T>(
    val item: T,
    val array: LineChartArray<T>,
)
