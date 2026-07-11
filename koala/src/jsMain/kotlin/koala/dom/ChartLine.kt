package koala.dom

data class ChartLine<T>(
    val name: String,
    val getX: (T) -> Double,
    val getY: (T) -> Double,
    val color: String? = null,
    val axisLabel: String = name
)

data class ChartData<T>(
    val points: List<T>,
    val lines: List<ChartLine<T>>
)