package streetlight.model.data

enum class NotationStyle {
    Diatonic,
    Nashville,
    // Jazz,
}

fun notationOf(
    interval: Int,
    root: Int = 1,
    mark: String? = null,
    style: NotationStyle = NotationStyle.Diatonic
) = when (style) {
    NotationStyle.Diatonic -> diatonicNotationOf(interval, root, mark)
    NotationStyle.Nashville -> nashvilleNotationOf(interval, mark)
}

fun diatonicNotationOf(interval: Int, root: Int, mark: String?): String {
    val intNote = ((root + interval - 2) % 7) + 1
    val note = when (intNote) {
        1 -> "C"
        2 -> "D"
        3 -> "E"
        4 -> "F"
        5 -> "G"
        6 -> "A"
        7 -> "B"
        else -> error("invalid intNote: $intNote")
    }
    val mark = when (mark) {
        null -> ""
        "-" -> "m"
        "7" -> "7"
        else -> error("invalid mark: $mark")
    }
    return note + mark
}

fun nashvilleNotationOf(interval: Int, mark: String?) = "$interval${mark ?: ""}"