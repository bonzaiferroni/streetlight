package streetlight.compose.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import kotlin.time.Instant
import koala.model.ChartData
import koala.model.ChartPoint as ModelChartPoint
import koala.model.MarkSeries
import koala.model.PointSeries
import koala.modifier.Palette
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import streetlight.compose.utility.collectAsState
import streetlight.compose.utility.toColor
import streetlight.compose.view.currentTheme

/**
 * A line chart that plots [config]'s arrays of values over a canvas, with animated entry, interactive hover
 * inspection, and optional axis labels.
 */
@Composable
fun <T> LineChart(
    config: LineChartConfig<T>,
    modifier: Modifier = Modifier,
    markSeries: List<MarkSeries> = emptyList(),
) {
    if (config.arrays.isEmpty()) return

    var animationTarget by remember { mutableFloatStateOf(if (config.isAnimated) 0f else 1f) }
    LaunchedEffect(Unit) {
        animationTarget = 1f
    }
    val animation by animateFloatAsState(animationTarget, tween(1200))
    val keyAnimations = remember { mutableStateMapOf<Any, Float>() }
    val liveKeys = config.arrays.mapNotNull { it.key }.toSet()
    LaunchedEffect(liveKeys) {
        for (key in liveKeys) {
            if (keyAnimations.contains(key)) continue
            val anim = Animatable(0f)
            launch {
                snapshotFlow { anim.value }.collect { v -> keyAnimations[key] = v }
            }
            launch {
                if (anim.value == 0f) {
                    anim.animateTo(1f, tween(1200))
                }
            }
        }
        keyAnimations.keys.retainAll(liveKeys)
    }
    val textRuler = rememberTextMeasurer()
    var chartLinesCache by remember { mutableStateOf<List<ChartLine<T>>?>(null) }

    var pointerTarget by remember { mutableStateOf<ChartPoint<T>?>(null) }
    var pointerTargetNow by remember { mutableStateOf<ChartPoint<T>?>(null) }
    var pointerTargetPrev by remember { mutableStateOf<ChartPoint<T>?>(null) }
    val pointerAnimatable = remember { Animatable(0f) }
    val focusAnimation by animateFloatAsState(if (pointerTarget != null) 1f else 0f)
    LaunchedEffect(pointerTarget) {
        config.onHoverPoint?.invoke(
            pointerTarget?.let { point ->
                HoverInfo(
                    item = point.value,
                    array = config.arrays[point.arrayIndex],
                )
            }
        )
        pointerAnimatable.snapTo(0f)
        pointerTargetPrev = pointerTargetNow
        pointerTargetNow = pointerTarget
        pointerAnimatable.animateTo(1f)
    }

    val density = LocalDensity.current
    val pointerMinDistanceSquared = remember {
        with(density) {
            val dist = ChartPointerTargetDistance.dp.toPx()
            dist * dist
        }
    }

    Box(
        modifier = modifier
            .drawWithCache {
                val chartScope = gatherChartScope(config, textRuler)
                val lines = chartScope.gatherChartLines(config.arrays, config.provideX)
                chartLinesCache = lines

                val leftAxisLabels = chartScope.gatherLeftAxisLabels()
                val rightAxisLabels = chartScope.gatherRightAxisLabels()
                val bottomAxisLabels = chartScope.gatherBottomAxisLabels()
                val horizontalRule = chartScope.gatherHorizontalRule()

                onDrawBehind {
                    horizontalRule?.let { drawHorizontalRule(it, chartScope, animation) }

                    leftAxisLabels?.let { drawAxisLabels(it, animation) }
                    rightAxisLabels?.let { drawAxisLabels(it, animation) }
                    bottomAxisLabels?.let { drawAxisLabels(it, animation) }

                    for (markGroup in markSeries) {
                        drawMarkLines(
                            marks = markGroup.marks,
                            dimensionX = chartScope.dimensionX,
                            chartScope = chartScope,
                            markColor = config.contentColor.copy(alpha = 0.5f),
                        )
                    }

                    drawChartLines(lines, pointerTargetNow, animation, keyAnimations, focusAnimation)
                    drawChartPoints(
                        lines = lines,
                        chartScope = chartScope,
                        animation = animation,
                        keyAnimations = keyAnimations,
                        pointerTarget = pointerTargetNow,
                        pointerTargetPrev = pointerTargetPrev,
                        pointerAnimation = pointerAnimatable.value,
                    )
                    drawFocusValue(
                        lineChartScope = chartScope,
                        pointerTarget = pointerTargetNow,
                        pointerAnimation = pointerAnimatable.value,
                    )
                }
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Exit -> {
                                pointerTarget = null
                            }
                            PointerEventType.Move -> {
                                val chartLines = chartLinesCache ?: continue
                                pointerTarget = detectPointerTarget(
                                    event.changes.first().position,
                                    chartLines,
                                    pointerMinDistanceSquared,
                                )
                            }
                        }
                    }
                }
            }
    )
}

/** Renders [dataFlow] as a reactive line chart with an accompanying legend. */
@Composable
fun LineChart(
    dataFlow: Flow<ChartData>,
    modifier: Modifier = Modifier,
    isTimeAxis: Boolean = true,
) {
    val data by dataFlow.collectAsState(null)
    data?.let { chartData ->
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier) {
            LineChart(chartData, Modifier.fillMaxWidth().weight(1f, fill = false), isTimeAxis)
            LineChartLegend(chartData)
        }
    }
}

/** Renders [data] as a line chart with an optional time axis. */
@Composable
fun LineChart(
    data: ChartData,
    modifier: Modifier = Modifier,
    isTimeAxis: Boolean = true,
) {
    val theme = currentTheme
    val pointSeries = data.series.filterIsInstance<PointSeries>()
    val markSeries = data.series.filterIsInstance<MarkSeries>()
    if (pointSeries.isEmpty() || pointSeries.all { it.points.isEmpty() }) return

    val allPoints = pointSeries.flatMap { it.points }
    val minX = allPoints.minOf { it.x }
    val maxX = allPoints.maxOf { it.x }

    val bottomAxis = if (isTimeAxis && maxX > minX) {
        val earliest = Instant.fromEpochMilliseconds(minX.toLong())
        val latest = Instant.fromEpochMilliseconds(maxX.toLong())
        BottomAxisConfig(generateTimeAxis(earliest, latest))
    } else {
        BottomAxisAutoConfig(5)
    }

    val arrays = pointSeries.mapIndexed { index, series ->
        val color = Palette.cycle(index).rgb.toColor()
        LineChartArray<ModelChartPoint>(
            values = series.points,
            color = color,
            label = series.name,
            axis = if (index == 0) SideAxisAutoConfig(5, AxisSide.Left) else null,
            provideY = { it.y },
        )
    }

    val config = LineChartConfig(
        arrays = arrays,
        contentColor = theme.colors.ink,
        glowColor = theme.colors.primary,
        bottomAxis = bottomAxis,
        provideX = { it.x },
    )

    LineChart(config, modifier, markSeries = markSeries)
}

/** A legend displaying the name and color indicator of each series in [data]. */
@Composable
fun LineChartLegend(
    data: ChartData,
    modifier: Modifier = Modifier,
) {
    val theme = currentTheme
    val pointSeries = data.series.filterIsInstance<PointSeries>()
    if (pointSeries.isEmpty()) return

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(theme.unit * 2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        pointSeries.forEachIndexed { index, series ->
            val color = Palette.cycle(index).rgb.toColor()
            Row(
                horizontalArrangement = Arrangement.spacedBy(theme.unit / 2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(theme.unit).clip(CircleShape).background(color)
                )
                BasicText(
                    text = series.name,
                    style = theme.text.small,
                    color = { theme.colors.ink },
                )
            }
        }
    }
}

internal fun <T> detectPointerTarget(
    pointerPos: Offset,
    chartLines: List<ChartLine<T>>,
    minDistanceSquared: Float,
): ChartPoint<T>? {
    var closestPoint: ChartPoint<T>? = null
    var closestDistance = Float.MAX_VALUE
    for (line in chartLines) {
        for (point in line.points) {
            val dist = distanceSquared(pointerPos, point.offset)
            if (dist > minDistanceSquared || dist > closestDistance) continue
            closestDistance = dist
            closestPoint = point
        }
    }
    return closestPoint
}

fun distanceSquared(a: Offset, b: Offset): Float {
    val dx = a.x - b.x
    val dy = a.y - b.y
    return dx * dx + dy * dy
}
