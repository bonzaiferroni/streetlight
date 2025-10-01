package streetlight.model.data

enum class Diatonic(val degree: Int, val letter: Char, val pitch: Int) {
    Do(1, 'C', 0),
    Re(2, 'D', 2),
    Mi(3, 'E', 4),
    Fa(4, 'F', 5),
    So(5, 'G', 7),
    La(6, 'A', 9),
    Ti(7, 'B', 11);

    fun toLabel(style: NotationStyle) = when (style) {
        NotationStyle.Letters -> letter
        NotationStyle.Nashville -> degree.toString()
    }

    companion object {
        fun fromDegree(degree: Int) = entries.firstOrNull { it.degree == degree }
            ?: error("Not a diatonic interval: $degree")
    }
}