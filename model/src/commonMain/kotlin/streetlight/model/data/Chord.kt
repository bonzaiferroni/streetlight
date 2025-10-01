package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class Chord(
    val pitch: Int,
    val quality: ChordQuality?,
    val extension: ChordExtension?,
    val slash: Int?,
) {
    val chromatic get() = Chromatic.ofPitch(pitch)

    fun toNotation(
        rootPitch: Int = 60,
        style: NotationStyle = NotationStyle.Letters
    ) = notationOf(this, rootPitch, style)

    companion object {
        fun ofNashville(
            degree: Int,
            quality: ChordQuality? = null,
            extension: ChordExtension? = null,
            slash: Int? = null,
        ) = Chord(
            pitch = Diatonic.fromDegree(degree).pitch,
            quality = quality,
            extension = extension,
            slash = slash,
        )
    }
}

@Serializable
data class MeasureChord(
    val duration: Int?,
    val expression: Chord?
) {
    fun toNotation(
        rootPitch: Int = 60,
        style: NotationStyle = NotationStyle.Letters
    ): String {
        val expressionNotation = expression?.toNotation(rootPitch, style)
        return duration?.let { duration ->
            "$expressionNotation:$duration"
        } ?: expressionNotation ?: "-"
    }

    companion object {
        fun ofNashville(
            degree: Int?,
            duration: Int? = null,
            quality: ChordQuality? = null,
            extension: ChordExtension? = null,
        ) = MeasureChord(duration, degree?.let { Chord.ofNashville(it, quality, extension) } )
    }
}

enum class ChordQuality(val letterNotation: String, val nashvilleNotation: String) {
    Major("maj", ""),        // C, D, E… (default, often no suffix)
    Minor("m", "-"),         // Cm, Dm, etc.
    Diminished("dim", "°"),  // Cdim or C°, Nashville uses the degree sign
    Augmented("aug", "+"),   // Caug or C+, Nashville uses plus
    Suspended("sus", "sus")  // Csus (usually 2 or 4 implied: Csus2, Csus4)
}

enum class ChordExtension(val notation: String) {
    Seventh("7"),
    MajorSeventh("maj7"),
    Sixth("6"),
    Ninth("9"),
    Eleventh("11"),
    Thirteenth("13")
}

fun parseMeasureChord(text: String, style: NotationStyle): MeasureChord? {
    if (text.contains(':')) {
        val array = text.split(':')
        if (array.size != 2) return null
        val duration = array[1].toIntOrNull() ?: return null
        if (array[0] == "-") return MeasureChord(duration, null)
        val chord = parseChord(array[0], style) ?: return null
        return MeasureChord(duration, chord)
    }
    if (text == "-") return MeasureChord(1, null)
    return parseChord(text, style)?.let { MeasureChord(1, it) }
}

fun parseChord(text: String, style: NotationStyle): Chord? {
    if (text.isBlank()) return null
    var index = 0
    val firstChar = text[index++]
    var pitch = when (style) {
        NotationStyle.Letters -> Diatonic.entries.firstOrNull { it.letter.equals(firstChar, true) }?.pitch
        NotationStyle.Nashville -> Diatonic.entries.firstOrNull { it.degree.toString()[0].equals(firstChar, true) }?.pitch
    } ?: return null
    if (text.length > index && text[index] == '#') {
        pitch++
        index++
    }
    var quality: ChordQuality? = null
    if (index < text.length) {
        for (qual in ChordQuality.entries) {
            val n = if (style == NotationStyle.Letters) qual.letterNotation else qual.nashvilleNotation
            if (index + n.length <= text.length && text.regionMatches(index, n, 0, n.length, ignoreCase = true)) {
                index += n.length
                quality = qual
                break
            }
        }
    }

    var extension: ChordExtension? = null
    if (index < text.length) {
        for (ext in ChordExtension.entries) {
            val n = ext.notation
            if (index + n.length <= text.length && text.regionMatches(index, n, 0, n.length, ignoreCase = true)) {
                index += n.length // advance if ye plan more parsing
                extension = ext
                break
            }
        }
    }
    var slash: Int? = null
    if (text.getOrNull(index) == '/') {
        val suffix = text.substring(++index)
        val slashChromatic = Chromatic.ofLabel(suffix) ?: return null
        index += suffix.length
        slash = slashChromatic.pitch
    }
    return if (index == text.length) { Chord(pitch, quality, extension, slash) } else null
}