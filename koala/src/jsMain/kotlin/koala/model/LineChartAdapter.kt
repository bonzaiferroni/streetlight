package koala.model

import koala.modifier.KoalaTheme
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
import web.html.HTMLElement

class LineChartAdapter(
    private val container: HTMLElement,
    private val title: String,
    private val windowSize: Int? = 100,
) {
    private val chart: EChartsInstance = ECharts.init(container, KoalaTheme.ThemeId)
    private val observer = ResizeObserver { _, _ -> chart.resize() }
    private var series: List<ChartSeries> = emptyList()

    init {
        observer.observe(container)
    }

    fun renderData(data: ChartData) {
        if (data.series.isEmpty()) return
        series = data.series.map { it.trimmed() }
        chart.setOption(getOption())
    }

    fun addPoint(slice: List<ChartPoint>) {
        if (slice.size != series.size) return
        series = series.mapIndexed { index, line ->
            line.copy(points = line.points + slice[index]).trimmed()
        }
        chart.setOption(getOption())
    }

    fun dispose() {
        observer.disconnect()
        chart.dispose()
    }

    private fun ChartSeries.trimmed() =
        windowSize?.takeIf { points.size > it }?.let { copy(points = points.takeLast(it)) } ?: this

    private fun getOption(): ChartOption {
        val axisLabels = series.map { it.axisLabel }.toSet()
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
            series = series.mapIndexed { index, line ->
                SeriesOption(
                    name = line.name,
                    type = "line",
                    showSymbol = false,
                    yAxisIndex = axisLabels.indexOf(line.axisLabel),
                    data = line.points.map { arrayOf(it.x, it.y) }.toTypedArray(),
                    lineStyle = LineStyleOption(color = line.color ?: ChartUtility.getLineColor(index), width = 2.0)
                )
            }.toTypedArray()
        )
    }
}
