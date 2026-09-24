package streetlight.model.data

/** How pitches are written: as letters, or as degrees of the scale. */
enum class NotationStyle(val label: String) {
    Letters("Letters"),
    Degrees("Degrees"),
}

/** [pitch] written in [style], relative to [rootPitch]. */
fun notationOf(
    pitch: Int,
    rootPitch: Int,
    style: NotationStyle = NotationStyle.Letters
) = when (style) {
    NotationStyle.Letters -> Chromatic.ofPitch(pitch).label
    NotationStyle.Degrees -> {
        val chromatic = Chromatic.ofPitch(pitch)
        if (chromatic.isSharp) "${chromatic.diatonic.degree}#"
        else chromatic.diatonic.degree.toString()
    }
}

/** [chord] written in [style], relative to [rootPitch]. */
fun notationOf(
    chord: Chord,
    rootPitch: Int = 60,
    style: NotationStyle = NotationStyle.Letters
) = when (style) {
    NotationStyle.Letters -> letterNotationOf(chord, rootPitch)
    NotationStyle.Degrees -> degreeNotationOf(chord, rootPitch)
}

/** [chord] written with letters, as "C#m7/E". */
fun letterNotationOf(chord: Chord, rootPitch: Int) = buildString {
    val chromatic = Chromatic.ofPitch(chord.pitch + rootPitch)
    append(chromatic.label)
    chord.quality?.let { append(it.letterNotation) }
    chord.extension?.let { append(it.notation) }
    chord.slash?.let { slash ->
        append('/')
        val slashChromatic = Chromatic.ofPitch(slash + rootPitch)
        append(slashChromatic.label)
    }
}

/** [chord] written with scale degrees. */
fun degreeNotationOf(chord: Chord, rootPitch: Int) = buildString {
    val chromatic = Chromatic.ofPitch(chord.pitch + rootPitch)
    append(chromatic.diatonic.degree)
    if (chromatic.isSharp) append("#")
    chord.quality?.let { append(it.degreeNotation) }
    chord.extension?.let { append(it.notation) }
    chord.slash?.let { slash ->
        append('/')
        val slashChromatic = Chromatic.ofPitch(slash + rootPitch)
        append(slashChromatic.diatonic.degree)
        if (slashChromatic.isSharp) append("#")
    }
}

/** The pitch [text] names in [style], or `null`. */
fun pitchOf(text: String, style: NotationStyle): Int? {
    val firstChar = text.getOrNull(0) ?: return null
    val pitch = when (style) {
        NotationStyle.Letters -> Diatonic.entries.firstOrNull { it.letter.equals(firstChar, true) }?.pitch
        NotationStyle.Degrees -> Diatonic.entries.firstOrNull { it.degree.toString()[0].equals(firstChar, true) }?.pitch
    } ?: return null
    val secondChar = text.getOrNull(1)
    return if (secondChar != null && secondChar == '#') {
        pitch + 1
    } else {
        pitch
    }
}