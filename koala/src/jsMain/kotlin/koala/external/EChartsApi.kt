package koala.external

import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement

@JsName("echarts")
external object ECharts {
    fun init(dom: HTMLElement, theme: String? = definedExternally, opts: InitOptions? = definedExternally): EChartsInstance
    fun registerTheme(name: String, theme: EChartsTheme)
    fun getInstanceByDom(dom: HTMLElement): EChartsInstance?
}

external interface EChartsInstance {
    fun setOption(option: dynamic)
    fun appendData(params: AppendDataParams)
    fun resize()
    fun dispose()
}

@JsPlainObject
external interface InitOptions {
    val renderer: String?
    val width: Int?
    val height: Int?
    val locale: String?
}

@JsPlainObject
external interface TitleOption {
    val text: String
}

@JsPlainObject
external interface ChartOption {
    val title: TitleOption?
    val tooltip: TooltipOption?
    val xAxis: AxisOption
    val yAxis: Array<AxisOption>
    val series: Array<SeriesOption>
}

@JsPlainObject
external interface TextStyleOption {
    val color: String?
    val fontFamily: String?
    val fontSize: Int?
    val fontWeight: String?
}

@JsPlainObject
external interface LineStyleOption {
    val color: String?
    val width: Double?
}

@JsPlainObject
external interface AreaStyleOption {
    val color: Array<String>?
}

@JsPlainObject
external interface AxisLineOption {
    val lineStyle: LineStyleOption?
}

@JsPlainObject
external interface SplitLineOption {
    val show: Boolean?
    val lineStyle: LineStyleOption?
}

@JsPlainObject
external interface SplitAreaOption {
    val areaStyle: AreaStyleOption?
}

@JsPlainObject
external interface ThemeAxisOption {
    val axisLine: AxisLineOption?
    val splitLine: SplitLineOption?
    val splitArea: SplitAreaOption?
    val minorSplitLine: SplitLineOption?
}

@JsPlainObject
external interface AxisPointerLabelOption {
    val color: String?
}

@JsPlainObject
external interface AxisPointerOption {
    val lineStyle: LineStyleOption?
    val crossStyle: LineStyleOption?
    val label: AxisPointerLabelOption?
}

@JsPlainObject
external interface ThemeTitleOption {
    val textStyle: TextStyleOption?
    val subtextStyle: TextStyleOption?
}

@JsPlainObject
external interface ThemeLegendOption {
    val textStyle: TextStyleOption?
}

@JsPlainObject
external interface ThemeLineOption {
    val symbol: String?
    val emphasis: EmphasisOption?
}

@JsPlainObject
external interface EChartsTheme {
    val darkMode: Boolean?
    val color: Array<String>?
    val backgroundColor: String?
    val textStyle: TextStyleOption?
    val title: ThemeTitleOption?
    val legend: ThemeLegendOption?
    val axisPointer: AxisPointerOption?
    val timeAxis: ThemeAxisOption?
    val valueAxis: ThemeAxisOption?
    val categoryAxis: ThemeAxisOption?
    val line: ThemeLineOption?
    val tooltip: ThemeTooltipOption?
}

@JsPlainObject
external interface EmphasisOption {
    val lineStyle: LineStyleOption?
    val focus: String?
}

@JsPlainObject
external interface ThemeTooltipOption {
    val backgroundColor: String?
    val borderColor: String?
    val borderWidth: Double?
    val textStyle: TextStyleOption?
}

@JsPlainObject
external interface TimeLabelFormatter {
    val year: String?
    val month: String?
    val day: String?
    val hour: String?
    val minute: String?
}

@JsPlainObject
external interface AxisLabelOption {
    val show: Boolean?
    val formatter: TimeLabelFormatter?
}

@JsPlainObject
external interface AppendDataParams {
    val seriesIndex: Int
    val data: Array<Array<Double>>
}

@JsPlainObject
external interface LinesDataItem {
    val coords: Array<Array<Double>>
}

@JsPlainObject
external interface AxisOption {
    val type: String
    val name: String?
    val min: Double?
    val max: Double?
    val axisLabel: AxisLabelOption?
    val splitLine: SplitLineOption?
    val position: String?
}

@JsPlainObject
external interface SeriesOption {
    val name: String?
    val type: String
    val coordinateSystem: String?
    val polyline: Boolean?
    val showSymbol: Boolean?
    val data: dynamic
    val lineStyle: LineStyleOption?
    val yAxisIndex: Int
}

@JsPlainObject
external interface TooltipOption {
    val trigger: String
    val valueFormatter: ((Number) -> String)?
}