package streetlight.compose.charts

import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class LineChartScope(
    override val config: ChartConfig,
    override val dimensionX: ChartDimension,
    val dimensionsY: List<ChartDimension>,
    override val leftAxis: ChartAxis?,
    override val rightAxis: ChartAxis?,
    override val bottomAxis: ChartAxis?,
    override val chartLeftMarginPx: Float,
    override val chartRightMarginPx: Float,
    override val chartTopMarginPx: Float,
    override val chartBottomMarginPx: Float,
    override val chartWidthPx: Float,
    override val chartHeightPx: Float,
    override val sizePx: Size,
    override val density: Float,
    override val axisMarginPx: Float,
    override val labelHeightPx: Float,
    override val labelPaddingPx: Float,
    override val labelFontSize: TextUnit,
    val lineWidthPx: Float,
    val pointRadiusPx: Float,
    override val textRuler: TextMeasurer,
    override val horizontalLineWidthPx: Float,
) : ChartScope

internal fun <T> CacheDrawScope.gatherChartScope(
    config: LineChartConfig<T>,
    textRuler: TextMeasurer,
): LineChartScope {
    val labelFontSize = ChartAxisLabelHeight.sp
    val labelHeightPx = labelFontSize.toPx()
    val bottomMarginPx = ChartBottomMargin.dp.toPx()
    val pointRadiusPx = 8.dp.toPx()

    // vertical space
    val chartBottomMarginPx = (if (config.bottomAxis != null) labelHeightPx * 1.5f else 0f) + pointRadiusPx
    val chartTopMarginPx = pointRadiusPx
    val chartHeightPx = (size.height - chartTopMarginPx - chartBottomMarginPx).coerceAtLeast(1f)

    val arrays = config.arrays
    val isSingularDimensionY = arrays.all { it.axis?.side != AxisSide.Left } ||
        arrays.all { it.axis?.side != AxisSide.Right }

    val dataScopes = arrays.map { c ->
        gatherDataScope(
            array = c.values,
            floor = c.floor,
            ceiling = c.ceiling,
            startX = config.startX,
            endX = config.endX,
            provideX = config.provideX,
            provideY = c.provideY,
        )
    }.let { scopes ->
        if (scopes.isEmpty()) {
            listOf(DataScope(1.0, 0.0, 1.0, 0.0))
        } else {
            val minX = scopes.minOf { it.minX }
            val maxX = scopes.maxOf { it.maxX }
            scopes.map { scope ->
                val minY = if (isSingularDimensionY) scopes.minOf { it.minY } else scope.minY
                val maxY = if (isSingularDimensionY) scopes.maxOf { it.maxY } else scope.maxY
                DataScope(maxX, minX, maxY, minY)
            }
        }
    }

    val dimensionsY = dataScopes.map { dataScope ->
        val dataRangeY = dataScope.rangeY.takeIf { it != 0.0 } ?: 1.0
        ChartDimension(
            scalePx = chartHeightPx / dataRangeY.toFloat(),
            max = dataScope.maxY,
            min = dataScope.minY,
        )
    }

    val leftAxis = gatherSideAutoAxis(AxisSide.Left, arrays, dimensionsY, textRuler, labelFontSize)
    val rightAxis = gatherSideAutoAxis(AxisSide.Right, arrays, dimensionsY, textRuler, labelFontSize)

    val chartLeftMarginPx = (leftAxis?.maxLabelWidthPx?.let { it + labelHeightPx / 2 } ?: 0f) + pointRadiusPx
    val chartRightMarginPx = (rightAxis?.maxLabelWidthPx?.let { it + labelHeightPx / 2 } ?: 0f) + pointRadiusPx
    val chartWidthPx = (size.width - chartRightMarginPx - chartLeftMarginPx).coerceAtLeast(1f)

    val dataMinX = dataScopes.minOfOrNull { it.minX } ?: 0.0
    val dataMaxX = dataScopes.maxOfOrNull { it.maxX } ?: 1.0
    val dataRangeX = (dataMaxX - dataMinX).takeIf { it > 0.0 } ?: 1.0
    val dimensionX = ChartDimension(scalePx = chartWidthPx / dataRangeX.toFloat(), max = dataMaxX, min = dataMinX)
    val bottomAxis = config.bottomAxis?.let {
        when (it) {
            is BottomAxisAutoConfig -> gatherAutoAxis(it, dimensionX, textRuler, config.contentColor, labelFontSize, config.provideLabelX)
            is BottomAxisConfig -> gatherAxis(it.ticks, dimensionX, textRuler, config.contentColor, labelFontSize)
        }
    }

    return LineChartScope(
        config = config,
        dimensionX = dimensionX,
        dimensionsY = dimensionsY,
        leftAxis = leftAxis,
        rightAxis = rightAxis,
        bottomAxis = bottomAxis,
        chartLeftMarginPx = chartLeftMarginPx,
        chartRightMarginPx = chartRightMarginPx,
        chartTopMarginPx = chartTopMarginPx,
        chartBottomMarginPx = chartBottomMarginPx,
        chartWidthPx = chartWidthPx,
        chartHeightPx = chartHeightPx,
        sizePx = size,
        density = density,
        axisMarginPx = bottomMarginPx,
        labelHeightPx = labelHeightPx,
        lineWidthPx = 3.dp.toPx(),
        pointRadiusPx = pointRadiusPx,
        labelPaddingPx = ChartAxisPadding.dp.toPx(),
        labelFontSize = labelFontSize,
        textRuler = textRuler,
        horizontalLineWidthPx = 2.dp.toPx(),
    )
}
