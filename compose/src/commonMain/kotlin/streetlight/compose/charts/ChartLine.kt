package streetlight.compose.charts

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import koala.model.ChartMark
import streetlight.compose.utility.mixSrgb

internal data class ChartLine<T>(
    val path: ChartPath,
    val points: List<ChartPoint<T>>,
    val key: Any?,
)

internal data class ChartPath(
    val path: Path,
    val stroke: Stroke,
    val brush: Brush,
)

internal data class ChartPoint<T>(
    val offset: Offset,
    val color: Color,
    val labelX: String,
    val labelY: String,
    val side: AxisSide,
    val arrayIndex: Int,
    val value: T,
)

internal fun <T> LineChartScope.gatherChartLines(
    arrays: List<LineChartArray<T>>,
    provideX: ((T) -> Double)?,
): List<ChartLine<T>> {
    val chartLines = mutableListOf<ChartLine<T>>()
    val (scalePxX, _, minX) = this.dimensionX
    arrays.forEachIndexed { arrayIndex, array ->
        val dimensionY = dimensionsY.getOrNull(arrayIndex) ?: return@forEachIndexed
        val (scalePxY, _, minY) = dimensionY
        val color = array.color
        val mixedColor = config.glowColor?.let { color.mixSrgb(it, 0.5f) } ?: color
        val path = Path()
        var prevX: Float? = null
        var prevY: Float? = null
        val arrayPoints = mutableListOf<ChartPoint<T>>()
        val sortedValues = array.values.sortedBy { provideX?.invoke(it) ?: 0.0 }
        sortedValues.forEachIndexed { i, value ->
            val valueX = provideX?.invoke(value) ?: i.toDouble()
            val valueY = array.provideY(value)
            val x = chartLeftMarginPx + valueOffsetPx(valueX, minX, scalePxX)
            val y = sizePx.height - chartBottomMarginPx - valueOffsetPx(valueY, minY, scalePxY)

            val ratio = if (dimensionY.range > 0f) ((valueY - minY).toFloat() / dimensionY.range).coerceIn(0f, 1f) else 0f
            arrayPoints.add(
                ChartPoint(
                    offset = Offset(x, y),
                    color = color.mixSrgb(mixedColor, ratio),
                    labelX = config.provideLabelX(valueX),
                    labelY = array.provideLabelY(valueY),
                    side = array.axis?.side ?: AxisSide.Left,
                    arrayIndex = arrayIndex,
                    value = value,
                )
            )
            if (i == 0) {
                path.moveTo(x, y)
            } else if (array.isBezier) {
                path.drawBezier(
                    prevX = prevX ?: x,
                    prevY = prevY ?: y,
                    currentX = x,
                    currentY = y,
                )
            } else {
                path.lineTo(x, y)
            }
            prevX = x
            prevY = y
        }
        val stroke = Stroke(
            width = lineWidthPx,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
        val brush = Brush.verticalGradient(
            colors = listOf(color, mixedColor),
            startY = chartHeightPx,
            endY = 0f,
        )
        chartLines.add(ChartLine(ChartPath(path, stroke, brush), arrayPoints, array.key))
    }
    return chartLines
}

internal fun <T> DrawScope.drawChartLines(
    lines: List<ChartLine<T>>,
    pointerTarget: ChartPoint<T>?,
    animation: Float,
    keyAnimations: Map<Any, Float>,
    focusAnimation: Float,
) {
    val alpha = (1 - focusAnimation) * 0.5f + 0.5f
    for (line in lines) {
        val lineAlpha = if (pointerTarget != null && line.points.contains(pointerTarget)) 0.9f else alpha
        val lineAnim = line.key?.let { keyAnimations[it] } ?: animation
        val (path, stroke, brush) = line.path
        if (lineAnim >= 1f) {
            drawPath(
                path = path,
                style = stroke,
                brush = brush,
                alpha = lineAlpha,
            )
        } else {
            val pathMeasure = PathMeasure().apply {
                setPath(path, false)
            }
            val currentLength = lineAnim * pathMeasure.length
            val partialPath = Path().apply {
                pathMeasure.getSegment(0f, currentLength, this, true)
            }
            drawPath(
                path = partialPath,
                brush = brush,
                style = stroke,
                alpha = lineAlpha,
            )
        }
    }
}

internal fun <T> DrawScope.drawChartPoints(
    lines: List<ChartLine<T>>,
    chartScope: LineChartScope,
    animation: Float,
    keyAnimations: Map<Any, Float>,
    pointerTarget: ChartPoint<T>?,
    pointerTargetPrev: ChartPoint<T>?,
    pointerAnimation: Float,
) {
    for (line in lines) {
        val lineAnim = line.key?.let { keyAnimations[it] } ?: animation
        line.points.forEachIndexed { i, point ->
            val indexAnimation = (-i + lineAnim * line.points.size).coerceIn(0f, 1f)
            val bump = if (pointerTarget == point) pointerAnimation * chartScope.pointRadiusPx
            else if (pointerTargetPrev == point) (1 - pointerAnimation) * chartScope.pointRadiusPx
            else 0f
            val radius = chartScope.pointRadiusPx * indexAnimation + bump

            drawCircle(
                color = point.color,
                radius = radius / 2,
                center = point.offset,
                alpha = indexAnimation * 0.6f,
            )
            drawCircle(
                color = chartScope.config.contentColor,
                radius = radius / 4,
                center = point.offset,
                alpha = indexAnimation * 0.6f,
            )
        }
    }
}

internal fun DrawScope.drawMarkLines(
    marks: List<ChartMark>,
    dimensionX: ChartDimension,
    chartScope: LineChartScope,
    markColor: Color,
) {
    for (mark in marks) {
        val x = chartScope.chartLeftMarginPx + valueOffsetPx(mark.x, dimensionX.min, dimensionX.scalePx)
        if (x in chartScope.chartLeftMarginPx..(chartScope.chartLeftMarginPx + chartScope.chartWidthPx)) {
            drawLine(
                color = markColor,
                start = Offset(x, chartScope.chartTopMarginPx),
                end = Offset(x, chartScope.sizePx.height - chartScope.chartBottomMarginPx),
                strokeWidth = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
            )
        }
    }
}

internal fun <T> DrawScope.drawFocusValue(
    lineChartScope: LineChartScope,
    pointerTarget: ChartPoint<T>?,
    pointerAnimation: Float,
) {
    pointerTarget?.let { target ->
        val labelLayoutY = lineChartScope.textRuler.measure(
            text = target.labelY,
            style = TextStyle(color = target.color, fontSize = lineChartScope.labelFontSize),
        )
        val x = when (target.side) {
            AxisSide.Left -> (lineChartScope.leftAxis?.maxLabelWidthPx ?: labelLayoutY.size.width.toFloat()) / 2 - labelLayoutY.size.width / 2f
            AxisSide.Right -> lineChartScope.sizePx.width - (lineChartScope.rightAxis?.maxLabelWidthPx ?: labelLayoutY.size.width.toFloat()) / 2 - labelLayoutY.size.width / 2f
        }
        drawText(
            textLayoutResult = labelLayoutY,
            topLeft = Offset(
                x = x,
                y = target.offset.y - labelLayoutY.size.height / 2f,
            ),
            alpha = pointerAnimation,
        )
    }
}

private fun Path.drawBezier(
    prevX: Float,
    prevY: Float,
    currentX: Float,
    currentY: Float,
) {
    val controlPointDiv = 2.2f
    val controlX1 = prevX + (currentX - prevX) / controlPointDiv
    val controlX2 = currentX - (currentX - prevX) / controlPointDiv

    cubicTo(
        controlX1, prevY,
        controlX2, currentY,
        currentX, currentY,
    )
}

internal fun valueOffsetPx(value: Double, minValue: Double, scalePx: Float) = ((value - minValue) * scalePx).toFloat()
