package streetlight.model.data

enum class NotationStyle(val label: String) {
    Letters("Letters"),
    Degrees("Degrees"),
}

fun notationOf(
    pitch: Int,
    rootPitch: Int,
    style: NotationStyle = NotationStyle.Letters
) = when (style) {
    NotationStyle.Letters -> Chromatic.ofPitch(pitch).label
    NotationStyle.Degrees -> {
        val chromatic = Chromatic.ofPitch(pitch + rootPitch)
        if (chromatic.isSharp) "${chromatic.diatonic.degree}#"
        else chromatic.diatonic.degree.toString()
    }
}

fun notationOf(
    chord: Chord,
    rootPitch: Int = 60,
    style: NotationStyle = NotationStyle.Letters
) = when (style) {
    NotationStyle.Letters -> letterNotationOf(chord, rootPitch)
    NotationStyle.Degrees -> degreeNotationOf(chord, rootPitch)
}

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