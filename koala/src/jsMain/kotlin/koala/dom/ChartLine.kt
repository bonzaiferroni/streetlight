package koala.dom

data class ChartLine<T>(
    val getX: (T) -> Double,
    val getY: (T) -> Double,
    val name: String? = null,
    val color: String? = null,
)

data class ChartData<T>(
    val points: List<T>,
    val lines: List<ChartLine<T>>
)