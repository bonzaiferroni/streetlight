package koala.model

import koala.css.KoalaTheme
import koala.dom.ChartUtility
import koala.dom.ResizeObserver
import koala.external.AppendDataParams
import koala.external.AxisOption
import koala.external.ChartOption
import koala.external.ECharts
import koala.external.EChartsInstance
import koala.external.SeriesOption
import koala.external.TitleOption
import koala.external.TooltipOption
import org.w3c.dom.HTMLElement

class LineChart<T>(
    private val container: HTMLElement,
    private val title: String,
    private val getX: (T) -> Double,
    private val getY: (T) -> Double,
) {
    private val chart: EChartsInstance = ECharts.init(container, KoalaTheme.ThemeId)
    private val observer = ResizeObserver { _, _ -> chart.resize() }

    init {
        observer.observe(container)
    }

    fun renderPoints(points: List<T>) {
        chart.setOption(getOption(points))
    }

    fun addPoint(point: T) {
        chart.appendData(
            AppendDataParams(
                seriesIndex = 0,
                data = arrayOf(toPointArray(point))
            )
        )
    }

    fun dispose() {
        observer.disconnect()
        chart.dispose()
    }

    private fun toPointArray(point: T) = arrayOf(
        getX(point),
        getY(point),
    )

    private fun getOption(points: List<T>) = ChartOption(
        title = TitleOption(text = title),
        tooltip = TooltipOption(trigger = "axis"),
        xAxis = ChartUtility.DefaultTimeAxis,
        yAxis = AxisOption(type = "value"),
        series = arrayOf(
            SeriesOption(
                type = "line",
                showSymbol = false,
                data = points.map(::toPointArray).toTypedArray()
            )
        )
    )
}