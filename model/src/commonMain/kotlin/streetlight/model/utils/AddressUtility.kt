package streetlight.model.utils

/**
 * This address with the abbreviations of its street line spelled out: its suffix, such as "Ave", and its direction,
 * such as "E", so that "14200 E Alameda Ave" becomes "14200 East Alameda Avenue". Every other word is kept as written.
 */
fun String.expandAddress(): String {
    val street = substringBefore(',')
    val words = street.trim().split(whitespaceRun).filter { it.isNotEmpty() }
    val start = if (words.firstOrNull()?.first()?.isDigit() == true) 1 else 0
    val end = words.indexOfFirst { it.isUnitWord() }.takeIf { it > start } ?: words.size
    val trailingDirection = end - start >= 3 && words[end - 1].addressKey() in directions

    val expanded = words.mapIndexed { index, word ->
        val key = word.addressKey()
        when {
            index == start && end - start >= 3 -> directions[key]
            index == end - 1 && trailingDirection -> directions[key]
            index == (if (trailingDirection) end - 2 else end - 1) && index > start -> suffixes[key]
            else -> null
        } ?: word
    }
    return expanded.joinToString(" ") + substring(street.length)
}

private fun String.addressKey() = trimEnd('.').lowercase()

private fun String.isUnitWord() = startsWith("#") || addressKey() in unitWords

private val whitespaceRun = Regex("""\s+""")

private val unitWords = setOf("unit", "suite", "ste", "apt", "apartment", "room", "rm", "building", "bldg")

private val directions = mapOf(
    "n" to "North", "s" to "South", "e" to "East", "w" to "West",
    "ne" to "Northeast", "nw" to "Northwest", "se" to "Southeast", "sw" to "Southwest",
)

private val suffixes = mapOf(
    "aly" to "Alley", "ave" to "Avenue", "av" to "Avenue", "blvd" to "Boulevard", "cir" to "Circle",
    "ct" to "Court", "cv" to "Cove", "dr" to "Drive", "expy" to "Expressway", "fwy" to "Freeway",
    "hwy" to "Highway", "ln" to "Lane", "pkwy" to "Parkway", "pl" to "Place", "plz" to "Plaza",
    "rd" to "Road", "sq" to "Square", "st" to "Street", "ter" to "Terrace", "trl" to "Trail",
    "xing" to "Crossing",
)
