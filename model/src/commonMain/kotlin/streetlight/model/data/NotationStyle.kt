package streetlight.model.data

enum class NotationStyle(val label: String) {
    Letters("Letters"),
    Nashville("Nashville"),
}

fun notationOf(
    pitch: Int,
    style: NotationStyle = NotationStyle.Letters
) = when (style) {
    NotationStyle.Letters -> Chromatic.ofPitch(pitch).label
    NotationStyle.Nashville -> {
        val chromatic = Chromatic.ofPitch(pitch)
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
    NotationStyle.Nashville -> nashvilleNotationOf(chord, rootPitch)
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

fun nashvilleNotationOf(chord: Chord, rootPitch: Int) = buildString {
    val chromatic = Chromatic.ofPitch(chord.pitch + rootPitch)
    append(chromatic.diatonic.degree)
    if (chromatic.isSharp) append("#")
    chord.quality?.let { append(it.nashvilleNotation) }
    chord.extension?.let { append(it.notation) }
    chord.slash?.let { slash ->
        append('/')
        val slashChromatic = Chromatic.ofPitch(slash + rootPitch)
        append(slashChromatic.diatonic.degree)
        if (slashChromatic.isSharp) append("#")
    }
}