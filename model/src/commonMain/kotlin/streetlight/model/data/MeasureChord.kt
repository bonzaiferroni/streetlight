package streetlight.model.data

import kotlinx.serialization.Serializable

/** A chord held for a [duration] of beats in a measure. A `null` chord is a rest. */
@Serializable
data class MeasureChord(
    val duration: Int?,
    val expression: Chord?,
    val isPhraseEnd: Boolean = false,
) {
    /** The chord and its duration written in [style], relative to [rootPitch]. */
    fun toNotation(
        rootPitch: Int = 60,
        style: NotationStyle = NotationStyle.Letters
    ): String {
        val expressionNotation = expression?.toNotation(rootPitch, style)
        return buildString {
            append(expressionNotation ?: "~")
            duration?.let {
                append('_')
                append(it)
            }
        }
    }

    companion object {
        fun ofDegree(
            degree: Int?,
            duration: Int? = null,
            quality: ChordQuality? = null,
            extension: ChordExtension? = null,
            isPhraseEnd: Boolean = false,
        ) = MeasureChord(
            duration = duration,
            expression = degree?.let { Chord.ofDegree(it, quality, extension) },
            isPhraseEnd = isPhraseEnd,
        )
    }
}

/** Reads a measure chord written by [MeasureChord.toNotation], or returns `null`. */
fun parseMeasureChord(text: String, style: NotationStyle, isPhraseEnd: Boolean): MeasureChord? {
    var text = text
    val duration = if (text.contains('_')) {
        val array = text.split('_')
        if (array.size != 2) return null
        val duration = array[1].toIntOrNull() ?: return null
        text = array[0]
        duration
    } else null
    if (text == "~") return MeasureChord(duration, null, isPhraseEnd)
    return parseChord(text, style)?.let { MeasureChord(duration, it, isPhraseEnd) }
}