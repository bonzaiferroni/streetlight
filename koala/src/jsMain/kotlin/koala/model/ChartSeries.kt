package koala.model

/** A named series of a chart. */
sealed interface ChartSeries {
    val name: String
    val color: String?
}

/** A point on a chart. */
data class ChartPoint(
    val x: Double,
    val y: Double,
)

/** A labeled vertical mark on a chart at [x]. */
data class ChartMark(
    val x: Double,
    val label: String,
)

/** A line of points, plotted against the axis named [axisLabel]. */
data class PointSeries(
    override val name: String,
    val points: List<ChartPoint>,
    override val color: String? = null,
    val axisLabel: String = name,
): ChartSeries

/** A series of vertical marks. */
data class MarkSeries(
    override val name: String,
    val marks: List<ChartMark>,
    override val color: String? = null,
): ChartSeries

/** The series a chart shows. */
data class ChartData(
    val series: List<ChartSeries>,
)

/** A [PointSeries] of [source], with each point read by [getX] and [getY]. */
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

/** A [MarkSeries] of [source], with each mark read by [getX] and [getLabel]. */
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
