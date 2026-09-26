package kampfire.utils

import kotlin.math.max
import kotlin.math.min

/**
 * The similarity of this text to [other], from 0 to 1, found by matching each word of the text with fewer
 * words to its closest word in the other, and averaging. Words are compared by Levenshtein distance, ignoring
 * case and every character but letters and digits.
 */
fun String.similarity(other: String): Double {
    val words = toWords()
    val otherWords = other.toWords()
    if (words.isEmpty() || otherWords.isEmpty()) return if (words == otherWords) 1.0 else 0.0
    val (fewer, more) = if (words.size <= otherWords.size) words to otherWords else otherWords to words
    return fewer.sumOf { word -> more.maxOf { word.wordSimilarity(it) } } / fewer.size
}

/** Whether this text and [other] name the same thing, with a [similarity] of at least [threshold]. */
fun String.fuzzyMatches(other: String, threshold: Double = fuzzyMatchThreshold): Boolean = similarity(other) >= threshold

const val fuzzyMatchThreshold = 0.8

private fun String.toWords(): List<String> = buildList {
    val word = StringBuilder()
    for (char in lowercase()) {
        if (char.isLetterOrDigit()) {
            word.append(char)
        } else if (word.isNotEmpty()) {
            add(word.toString())
            word.clear()
        }
    }
    if (word.isNotEmpty()) add(word.toString())
}

private fun String.wordSimilarity(other: String): Double {
    val longest = max(length, other.length)
    if (longest == 0) return 1.0
    return 1.0 - levenshtein(other).toDouble() / longest
}

private fun String.levenshtein(other: String): Int {
    var previous = IntArray(other.length + 1) { it }
    var current = IntArray(other.length + 1)
    for (i in 1..length) {
        current[0] = i
        for (j in 1..other.length) {
            val cost = if (this[i - 1] == other[j - 1]) 0 else 1
            current[j] = min(min(current[j - 1] + 1, previous[j] + 1), previous[j - 1] + cost)
        }
        val swap = previous
        previous = current
        current = swap
    }
    return previous[other.length]
}
