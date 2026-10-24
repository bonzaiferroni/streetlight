package streetlight.compose.svg

/** The numbers of [text] in order, as SVG writes them, such as `-.5`, `1e-3` and the `.5` after `0.5`. */
internal fun numbersOf(text: String): List<Float> = numberRegex.findAll(text).mapNotNull { it.value.toFloatOrNull() }.toList()

/**
 * The value [fraction] of the way from [from] to [to], interpolating their numbers in place within [from]'s text.
 *
 * It serves path data, transform values and dash arrays alike. Values whose numbers do not pair up switch halfway.
 */
internal fun lerpValue(from: String, to: String, fraction: Float): String {
    if (fraction == 0f) return from
    val toNumbers = numbersOf(to)
    if (numberRegex.findAll(from).count() != toNumbers.size) return if (fraction < 0.5f) from else to

    var index = 0
    return numberRegex.replace(from) { match ->
        val start = match.value.toFloat()
        val end = toNumbers[index++]
        // a leading space keeps numbers SVG writes without separators, such as `0.5.5`, apart
        " " + (start + (end - start) * fraction)
    }
}

private val numberRegex = Regex("""[-+]?(?:\d+\.?\d*|\.\d+)(?:[eE][-+]?\d+)?""")
