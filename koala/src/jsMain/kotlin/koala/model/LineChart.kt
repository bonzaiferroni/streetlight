package koala.model

import koala.css.KoalaTheme
import koala.dom.ResizeObserver
import koala.external.AxisOption
import koala.external.ChartOption
import koala.external.ECharts
import koala.external.EChartsInstance
import koala.external.SeriesOption
import koala.external.TitleOption
import koala.external.TooltipOption
import org.w3c.dom.HTMLElement
import kotlin.time.Instant

class LineChart<T>(
    private val container: HTMLElement,
    private val title: String,
    private val toXValue: (T) -> Double,
    private val toYValue: (T) -> Double,
) {
    private val chart: EChartsInstance = ECharts.init(container, KoalaTheme.ThemeId)
    private val observer = ResizeObserver { _, _ -> chart.resize() }

    init {
        observer.observe(container)
    }

    fun render(points: List<T>) {
        chart.setOption(
            ChartOption(
                title = TitleOption(text = title),
                tooltip = TooltipOption(trigger = "axis"),
                xAxis = AxisOption(type = "time"),
                yAxis = AxisOption(type = "value"),
                series = arrayOf(
                    SeriesOption(
                        type = "line",
                        showSymbol = false,
                        data = points.map {
                            arrayOf(
                                toXValue(it),
                                toYValue(it),
                            )
                        }.toTypedArray()
                    )
                )
            )
        )
    }

    fun dispose() {
        observer.disconnect()
        chart.dispose()
    }
}