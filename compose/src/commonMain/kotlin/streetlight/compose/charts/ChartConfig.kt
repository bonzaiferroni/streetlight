package streetlight.compose.charts

import androidx.compose.ui.graphics.Color
import kabinet.utils.toMetricString

const val ChartPointerTargetDistance = 30
const val ChartBottomMargin = 24
const val ChartAxisLabelHeight = 12
const val ChartAxisPadding = ChartAxisLabelHeight / 2

interface ChartConfig {
    val contentColor: Color
    val isAnimated: Boolean
    val glowColor: Color?
    val bottomAxis: AxisConfig.Bottom?
    val startX: Double?
    val endX: Double?
    val provideLabelX: (Double) -> String
}

data class DataScope(
    val maxX: Double,
    val minX: Double,
    val maxY: Double,
    val minY: Double,
) {
    val rangeX get() = maxX - minX
    val rangeY get() = maxY - minY
}

data class ChartDimension(
    val scalePx: Float,
    val max: Double,
    val min: Double,
) {
    val range get() = (max - min).toFloat()
}

internal fun <T> gatherDataScope(
    array: List<T>,
    floor: Double?,
    ceiling: Double?,
    startX: Double?,
    endX: Double?,
    provideX: ((T) -> Double)?,
    provideY: (T) -> Double,
): DataScope {
    var minX = startX ?: Double.MAX_VALUE
    var maxX = endX ?: -Double.MAX_VALUE
    var minY = floor ?: Double.MAX_VALUE
    var maxY = ceiling ?: -Double.MAX_VALUE

    array.forEachIndexed { index, value ->
        val x = provideX?.invoke(value) ?: index.toDouble()
        val y = provideY(value)
        if (x < minX) minX = x
        if (x > maxX) maxX = x
        if (y < minY) minY = y
        if (y > maxY) maxY = y
    }

    if (minX == Double.MAX_VALUE) minX = 0.0
    if (maxX == -Double.MAX_VALUE) maxX = 1.0
    if (minY == Double.MAX_VALUE) minY = 0.0
    if (maxY == -Double.MAX_VALUE) maxY = 1.0

    return DataScope(maxX, minX, maxY, minY)
}
