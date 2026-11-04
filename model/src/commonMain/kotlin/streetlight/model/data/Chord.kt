package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class Chord(
    val root: Chroma,
    val quality: ChordQuality,
    val seventh: Seventh?,
    val extension: Extension?,
    val alterations: Set<Alteration>,
    val additions: Set<Addition>,
    val bass: Chroma?,
)

/** The quality of a chord, with its letter and degree notation. */
enum class ChordQuality(val letterNotation: String, val symbolNotation: String) {
    Major("maj", ""),        // C, D, E… (default, often no suffix)
    Minor("m", "-"),         // Cm, Dm, etc.
    Diminished("dim", "°"),  // Cdim or C°
    Augmented("aug", "+"),   // Caug or C+
    Sus2("sus2", "sus2"),
    Sus4("sus4", "sus4");
}

enum class Seventh {
    Minor,      // ♭7, as in C7 and Cm7
    Major,      // Cmaj7, Cm(maj7)
    Diminished, // C°7
}

/** The highest extension; it implies those below it, so C13 carries the 9 and 11. */
enum class Extension {
    Ninth,
    Eleventh,
    Thirteenth,
}

enum class Alteration {
    Flat5,
    Sharp5,
    Flat9,
    Sharp9,
    Sharp11,
    Flat13,
}

/** Tones added without implying a seventh. */
enum class Addition {
    Sixth,
    Ninth,
    Eleventh,
}