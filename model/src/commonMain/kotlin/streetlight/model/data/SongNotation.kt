package streetlight.model.data

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

@Stable
@Serializable
data class SongNotation(
    val root: Int,
    val measureBeats: Int,
    val measureTime: Int,
    val parts: List<SongPart>,
)

@Serializable
data class SongPart(
    val instrument: Instrument,
    val style: NotationStyle = NotationStyle.Diatonic,
    val sections: List<SongSection>,
)

@Serializable
data class SongSection(
    val title: String,
    val repetitions: Int = 1,
    val phrases: List<SongPhrase>,
) {
    fun toLabel() = repetitions.takeIf { it > 1 }?.let { "$title (${it}x)" } ?: title

    fun getMeasureCount(measureBeats: Int): Int {
        return phrases.sumOf { it.getMeasureCount(measureBeats) * it.repetitions }
    }

    fun getNoteCount() = phrases.sumOf { phrase ->  phrase.notes.size * phrase.repetitions }

    fun getNote(noteIndex: Int): SongNote {
        var notesBeforePhrase = 0
        phrases.forEach { phrase ->
            repeat(phrase.repetitions) {
                val phraseIndex = noteIndex - notesBeforePhrase
                if (phraseIndex < phrase.notes.size) return phrase.notes[phraseIndex]
                notesBeforePhrase += phrase.notes.size
            }
        }
        error("noteIndex out of bounds: $noteIndex")
    }
}

@Serializable
data class SongPhrase(
    val repetitions: Int = 1,
    val notes: List<SongNote>,
) {
    fun getMeasureCount(measureBeats: Int): Int {
        return (((notes.first().beat - 1) + notes.sumOf { it.duration }) / measureBeats) * repetitions
    }
}

@Serializable
data class SongNote(
    val interval: Int?,
    val beat: Int,
    val duration: Int,
    val mark: String? = null,
) {
    val isRest get() = interval == null

    fun getResolution(measureBeats: Int, measureTiming: Int): Int {
        if (beat == 1 && duration == 4) return 1
        if (beat == 3 && duration == 2) return 2
        else return 4
    }
}

enum class Instrument {
    RhythmGuitar,
    Voice,
}

fun SongPart.getResolution(measureBeats: Int, measureTiming: Int): Int {
    var resolution = 1
    sections.forEach { section ->
        section.phrases.forEach { phrase ->
            phrase.notes.forEach { note ->
                val noteResolution = note.getResolution(measureBeats, measureTiming)
                resolution = maxOf(noteResolution, resolution)
            }
        }
    }
    return resolution
}

val amazingGrace = SongNotation(
    root = 5,
    measureBeats = 4,
    measureTime = 4,
    parts = listOf(
        SongPart(
            instrument = Instrument.RhythmGuitar,
            sections = listOf(
                SongSection(
                    title = "Verse",
                    repetitions = 3,
                    phrases = listOf(
                        SongPhrase(
                            repetitions = 1,
                            notes = listOf(
                                // G, G7, C, G
                                SongNote(1, 1, 4),
                                SongNote(1, 1, 4, "7"),
                                SongNote(4, 1, 4),
                                SongNote(1, 1, 4),
                                // G, Am, D, D,
                                SongNote(1, 1, 4),
                                SongNote(2, 1, 4, "-"),
                                SongNote(5, 1, 4),
                                SongNote(5, 1, 4),
                                // G, G7, C, G
                                SongNote(1, 1, 4),
                                SongNote(1, 1, 4, "7"),
                                SongNote(4, 1, 4),
                                SongNote(1, 1, 4),
                                // Em, D, G, G,
                                SongNote(6, 1, 4, "-"),
                                SongNote(5, 1, 4),
                                SongNote(1, 1, 4),
                                SongNote(1, 1, 4),
                            ),
                        )
                    )
                )
            )
        )
    ),
)

