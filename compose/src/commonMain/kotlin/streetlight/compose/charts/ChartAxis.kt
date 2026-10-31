package streetlight.compose.charts

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit

@Stable
@Immutable
data class ChartAxis(
    val values: List<AxisValue>,
    val color: Color,
    val dimension: ChartDimension,
    val maxLabelWidthPx: Float,
)

data class AxisValue(
    val value: Double,
    val label: String,
    val layout: TextLayoutResult,
)

internal data class ChartAxisLine(
    val start: Offset,
    val end: Offset,
    val brush: Brush,
)

fun <T> gatherSideAutoAxis(
    side: AxisSide,
    arrays: List<LineChartArray<T>>,
    dimensionsY: List<ChartDimension>,
    textRuler: TextMeasurer,
    labelFontSize: TextUnit,
): ChartAxis? = arrays.withIndex().firstOrNull { it.value.axis?.side == side }?.let { indexedArray ->
    val (index, array) = indexedArray
    val axisConfig = array.axis!!
    val color = axisConfig.color ?: array.color
    val dimension = dimensionsY[index]
    gatherAutoAxis(axisConfig, dimension, textRuler, color, labelFontSize, array.provideLabelY)
}

fun gatherAutoAxis(
    axisConfig: AxisConfig,
    dimension: ChartDimension,
    textRuler: TextMeasurer,
    color: Color,
    fontSize: TextUnit,
    toLabel: (Double) -> String,
): ChartAxis {
    val count = axisConfig.tickCount.coerceAtLeast(2)
    val values = (0 until count).map { tickIndex ->
        val value = tickIndex * (dimension.range / (count - 1)) + dimension.min
        val label = toLabel(value)
        val layout = textRuler.measure(
            text = label,
            style = TextStyle(color = color, fontSize = fontSize),
        )
        AxisValue(value, label, layout)
    }
    return ChartAxis(
        values = values,
        color = color,
        dimension = dimension,
        maxLabelWidthPx = values.maxOfOrNull { it.layout.size.width }?.toFloat() ?: 0f,
    )
}

fun gatherAxis(
    ticks: List<AxisTick>,
    dimension: ChartDimension,
    textRuler: TextMeasurer,
    color: Color,
    fontSize: TextUnit,
): ChartAxis {
    val values = ticks.map { tick ->
        val layout = textRuler.measure(
            text = tick.label,
            style = TextStyle(color = color, fontSize = fontSize),
        )
        AxisValue(tick.value, tick.label, layout)
    }
    return ChartAxis(
        values = values,
        color = color,
        dimension = dimension,
        maxLabelWidthPx = values.maxOfOrNull { it.layout.size.width }?.toFloat() ?: 0f,
    )
}
