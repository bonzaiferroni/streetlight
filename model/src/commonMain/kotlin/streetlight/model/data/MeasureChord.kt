package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class MeasureChord(
    val duration: Int?,
    val expression: Chord?,
    val isPhraseEnd: Boolean = false,
) {
    fun toNotation(
        rootPitch: Int = 60,
        style: NotationStyle = NotationStyle.Letters
    ): String {
        val expressionNotation = expression?.toNotation(rootPitch, style)
        return buildString {
            append(expressionNotation ?: "-")
            duration?.let {
                append('.')
                append(it)
            }
        }
    }

    companion object {
        fun ofNashville(
            degree: Int?,
            duration: Int? = null,
            quality: ChordQuality? = null,
            extension: ChordExtension? = null,
            isPhraseEnd: Boolean = false,
        ) = MeasureChord(
            duration = duration,
            expression = degree?.let { Chord.ofNashville(it, quality, extension) },
            isPhraseEnd = isPhraseEnd,
        )
    }
}

fun parseMeasureChord(text: String, style: NotationStyle, isPhraseEnd: Boolean): MeasureChord? {
    var text = text
    val duration = if (text.contains('.')) {
        val array = text.split('.')
        if (array.size != 2) return null
        val duration = array[1].toIntOrNull() ?: return null
        text = array[0]
        duration
    } else null
    if (text == "-") return MeasureChord(duration, null, isPhraseEnd)
    return parseChord(text, style)?.let { MeasureChord(duration, it, isPhraseEnd) }
}