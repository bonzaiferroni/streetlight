package koala.model

data class ChartPoint(
    val x: Double,
    val y: Double,
)

data class ChartSeries(
    val name: String,
    val points: List<ChartPoint>,
    val color: String? = null,
    val axisLabel: String = name,
)

data class ChartData(
    val series: List<ChartSeries>,
)

fun <T> seriesOf(
    name: String,
    source: List<T>,
    getX: (T) -> Double,
    getY: (T) -> Double,
    color: String? = null,
    axisLabel: String = name,
) = ChartSeries(
    name = name,
    points = source.map { ChartPoint(getX(it), getY(it)) },
    color = color,
    axisLabel = axisLabel,
)
