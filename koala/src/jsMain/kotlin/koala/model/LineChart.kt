package koala.model

import koala.css.KoalaTheme
import koala.dom.ChartData
import koala.dom.ChartLine
import koala.dom.ChartUtility
import koala.dom.ResizeObserver
import koala.external.AxisLabelOption
import koala.external.AxisOption
import koala.external.ChartOption
import koala.external.ECharts
import koala.external.EChartsInstance
import koala.external.LineStyleOption
import koala.external.SeriesOption
import koala.external.SplitLineOption
import koala.external.TitleOption
import koala.external.TooltipOption
import org.w3c.dom.HTMLElement

class LineChart<T>(
    private val container: HTMLElement,
    private val title: String,
    private val windowSize: Int? = 100,
) {
    private val chart: EChartsInstance = ECharts.init(container, KoalaTheme.ThemeId)
    private val observer = ResizeObserver { _, _ -> chart.resize() }
    private val cache = ArrayDeque<T>()
    private var lines: List<ChartLine<T>> = emptyList()

    init {
        observer.observe(container)
    }

    fun renderData(data: ChartData<T>) {
        cache.clear()
        data.points.forEach { cache.addFirst(it) }
        if (cache.isEmpty() || data.lines.isEmpty()) return
        lines = data.lines
        windowSize?.let { while (cache.size > it) cache.removeFirst() }
        chart.setOption(getOption())
    }

    fun addPoint(point: T) {
        cache.addLast(point)
        windowSize?.let { while (cache.size > it) cache.removeFirst() }
        chart.setOption(getOption())
    }

    fun dispose() {
        observer.disconnect()
        chart.dispose()
    }

    private fun toPointArray(point: T, line: ChartLine<T>) = arrayOf(line.getX(point), line.getY(point))

    private fun getOption(): ChartOption {
        val axisLabels = lines.map { it.axisLabel }.toSet()
        return ChartOption(
            title = TitleOption(text = title),
            tooltip = TooltipOption(trigger = "axis") { it.asDynamic().toFixed(1) as String },
            xAxis = ChartUtility.DefaultTimeAxis,
            yAxis = axisLabels.mapIndexed { index, label ->
                AxisOption(
                    type = "value",
                    name = label.takeIf { index < 2 },
                    splitLine = SplitLineOption(show = index == 0),
                    axisLabel = AxisLabelOption(show = index < 2),
                    position = when(index) {
                        0 -> "left"
                        1 -> "right"
                        else -> null
                    }
                )
            }.toTypedArray(),
//        yAxis = lines.mapIndexed { index, line ->
//            AxisOption(
//                type = "value",
//                name = line.name?.takeIf { index < 2 },
//                splitLine = SplitLineOption(show = index == 0),
//                axisLabel = AxisLabelOption(show = index < 2),
//                position = when(index) {
//                    0 -> "left"
//                    1 -> "right"
//                    else -> null
//                }
//            )
//        }.toTypedArray(),
            series = lines.mapIndexed { index, line ->
                SeriesOption(
                    name = line.name,
                    type = "line",
                    showSymbol = false,
                    yAxisIndex = axisLabels.indexOf(line.axisLabel),
                    data = cache.map({ toPointArray(it, line) }).toTypedArray(),
                    lineStyle = LineStyleOption(color = line.color ?: ChartUtility.getLineColor(index), width = 2.0)
                )
            }.toTypedArray()
        )
    }
}