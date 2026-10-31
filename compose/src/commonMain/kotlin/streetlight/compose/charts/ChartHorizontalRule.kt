package streetlight.compose.charts

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StampedPathEffectStyle
import androidx.compose.ui.graphics.drawscope.DrawScope

internal data class ChartHorizontalRule(
    val stamp: PathEffect,
    val lines: List<ChartAxisLine>,
)

internal fun ChartScope.gatherHorizontalRule(): ChartHorizontalRule? {
    val leftAxis = this.leftAxis ?: return null
    val startColor = leftAxis.color.copy(alpha = 0.35f)
    val endColor = (rightAxis?.color ?: leftAxis.color).copy(alpha = 0.35f)
    val scaleY = leftAxis.dimension.scalePx
    val minY = leftAxis.dimension.min

    return ChartHorizontalRule(
        stamp = PathEffect.stampedPathEffect(
            shape = Path().apply {
                val dotSize = horizontalLineWidthPx
                addOval(Rect(0f, 0f, dotSize, dotSize))
            },
            advance = horizontalLineWidthPx * 2.5f,
            phase = 0f,
            style = StampedPathEffectStyle.Translate,
        ),
        lines = leftAxis.values.map { axisValue ->
            val valueOffset = valueOffsetPx(axisValue.value, minY, scaleY)
            val height = sizePx.height - chartBottomMarginPx - valueOffset - horizontalLineWidthPx / 2
            ChartAxisLine(
                start = Offset(x = chartLeftMarginPx, y = height),
                end = Offset(x = chartLeftMarginPx + chartWidthPx, y = height),
                brush = Brush.horizontalGradient(
                    colors = listOf(startColor, endColor),
                    startX = chartLeftMarginPx,
                    endX = chartLeftMarginPx + chartWidthPx,
                ),
            )
        },
    )
}

internal fun DrawScope.drawHorizontalRule(
    rule: ChartHorizontalRule,
    lineChartScope: ChartScope,
    animation: Float,
) {
    rule.lines.forEachIndexed { i, line ->
        val alpha = (-i + animation * rule.lines.size).coerceIn(0f, 1f)
        drawLine(
            brush = line.brush,
            start = line.start,
            end = line.end,
            strokeWidth = lineChartScope.horizontalLineWidthPx,
            pathEffect = rule.stamp,
            alpha = alpha,
        )
    }
}
