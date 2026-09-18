package koala.model

sealed interface ChartSeries {
    val name: String
    val color: String?
}

data class ChartPoint(
    val x: Double,
    val y: Double,
)

data class ChartMark(
    val x: Double,
    val label: String,
)

data class PointSeries(
    override val name: String,
    val points: List<ChartPoint>,
    override val color: String? = null,
    val axisLabel: String = name,
): ChartSeries

data class MarkSeries(
    override val name: String,
    val marks: List<ChartMark>,
    override val color: String? = null,
): ChartSeries

data class ChartData(
    val series: List<ChartSeries>,
)

fun <T> pointSeriesOf(
    name: String,
    source: List<T>,
    getX: (T) -> Double,
    getY: (T) -> Double,
    color: String? = null,
    axisLabel: String = name,
) = PointSeries(
    name = name,
    points = source.map { ChartPoint(getX(it), getY(it)) },
    color = color,
    axisLabel = axisLabel,
)

fun <T> markSeriesOf(
    name: String,
    source: List<T>,
    getX: (T) -> Double,
    getLabel: (T) -> String,
    color: String? = null,
) = MarkSeries(
    name = name,
    marks = source.map { ChartMark(getX(it), getLabel(it)) },
    color = color,
)
