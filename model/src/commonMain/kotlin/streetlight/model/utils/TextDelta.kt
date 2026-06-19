package streetlight.model.utils

import kotlin.jvm.JvmInline

data class TextDelta(
    val addedLength: Int,
    val removedLength: Int,
    val segments: List<DeltaText>
) {
    companion object {
        val Empty get() = TextDelta(0, 0, emptyList())

        fun allAdded(content: String) = TextDelta(content.length, 0, listOf(AddedText(content)))
        fun allRemoved(content: String) = TextDelta(0, content.length, listOf(RemovedText(content)))
        fun allCommon(content: String) = TextDelta(content.length, content.length, listOf(CommonText(content)))
    }
}

sealed interface DeltaText {
    val text: String
}

@JvmInline
value class CommonText(override val text: String) : DeltaText

@JvmInline
value class RemovedText(override val text: String) : DeltaText

@JvmInline
value class AddedText(override val text: String) : DeltaText

enum class TextDeltaDisplay {
    Old,
    New,
    Combined,
}

fun createTextDelta(old: String?, new: String?): TextDelta {
    if (old.isNullOrEmpty() && new.isNullOrEmpty()) return TextDelta.Empty
    if (old.isNullOrEmpty() && !new.isNullOrEmpty()) return TextDelta.allAdded(new)
    if (new.isNullOrEmpty() && !old.isNullOrEmpty()) return TextDelta.allRemoved(old)
    if (old == new) TextDelta.allCommon(old!!)
    requireNotNull(old)
    requireNotNull(new)

    val n = old.length
    val m = new.length
    val max = n + m
    val trace = mutableListOf<IntArray>()

    // Forward pass — explore the edit graph, find shortest path
    for (d in 0..max) {
        val v = if (d == 0) {
            IntArray(2 * max + 1).also { it[max + 1] = 0 }
        } else {
            trace.last().copyOf()
        }

        for (k in -d..d step 2) {
            var x = if (k == -d || (k != d && v[max + k - 1] < v[max + k + 1])) {
                v[max + k + 1]       // move down (insertion)
            } else {
                v[max + k - 1] + 1   // move right (deletion)
            }
            var y = x - k

            // Follow the diagonal — matching characters are free
            while (x < n && y < m && old[x] == new[y]) {
                x++
                y++
            }

            v[max + k] = x

            if (x >= n && y >= m) {
                trace.add(v)
                return backtrack(trace, old, new, max)
            }
        }

        trace.add(v)
    }

    error("Myers diff failed — should be unreachable")
}

private fun backtrack(
    trace: List<IntArray>,
    old: String,
    new: String,
    max: Int
): TextDelta {
    val edits = mutableListOf<Pair<Char, Char>>() // (type, character)
    var x = old.length
    var y = new.length

    for (d in trace.size - 1 downTo 1) {
        val v = trace[d - 1]
        val k = x - y

        val prevK = if (k == -d || (k != d && v[max + k - 1] < v[max + k + 1])) {
            k + 1
        } else {
            k - 1
        }

        val prevX = v[max + prevK]
        val prevY = prevX - prevK

        // Retrace diagonal (matches)
        while (x > prevX && y > prevY) {
            x--; y--
            edits.add('=' to old[x])
        }

        // The non-diagonal step
        if (x == prevX) {
            y--
            edits.add('+' to new[y])
        } else {
            x--
            edits.add('-' to old[x])
        }
    }

    // Any remaining diagonal at d=0
    while (x > 0 && y > 0) {
        x--; y--
        edits.add('=' to old[x])
    }

    edits.reverse()

    // Collapse consecutive edits into DiffText spans
    val result = mutableListOf<DeltaText>()
    val buffer = StringBuilder()
    var currentType: Char? = null
    var addedCount = 0
    var removedCount = 0

    for ((type, char) in edits) {
        if (type == '+') addedCount++
        if (type == '-') removedCount++
        if (type != currentType) {
            if (buffer.isNotEmpty()) {
                result.add(currentType!!.toDiffText(buffer.toString()))
                buffer.clear()
            }
            currentType = type
        }
        buffer.append(char)
    }

    if (buffer.isNotEmpty() && currentType != null) {
        result.add(currentType.toDiffText(buffer.toString()))
    }

    return TextDelta(addedCount, removedCount, result)
}

private fun Char.toDiffText(text: String): DeltaText = when (this) {
    '=' -> CommonText(text)
    '-' -> RemovedText(text)
    '+' -> AddedText(text)
    else -> error("Unknown edit type: $this")
}