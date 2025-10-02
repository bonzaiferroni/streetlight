package streetlight.model.data

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import kotlin.collections.listOf

@Stable
@Serializable
data class SongNotation(
    val rootPitch: Int,
    val beatsPerMeasure: Int,
    val beatValue: Int,
    val parts: List<SongPart>,
) {
    companion object {
        val Empty get() = SongNotation(
            rootPitch = 60,
            beatsPerMeasure = 4,
            beatValue = 4,
            parts = listOf(SongPart.Empty),
        )
    }
}

@Serializable
data class SongPart(
    val instrument: Instrument,
    val style: NotationStyle = NotationStyle.Letters,
    val sections: List<SongSection> = emptyList(),
    val composition: List<Int> = listOf(0),
) {
    companion object {
        val Empty get() = SongPart(
            instrument = Instrument.RhythmGuitar,
            sections = listOf(SongSection.Empty),
            composition = listOf(0)
        )
    }
}

@Serializable
data class SongSection(
    val title: String,
    val repetitions: Int = 1,
    val chords: List<MeasureChord>,
) {
    fun toLabel() = repetitions.takeIf { it > 1 }?.let { "$title (${it}x)" } ?: title

//    fun getMeasureCount(beatsPerMeasure: Int): Int {
//        return chords.sumOf { it.duration } / beatsPerMeasure
//    }

    fun getChordAt(index: Int) = chords[index % chords.size]

    companion object {
        val Empty get() = SongSection(
            title = "Verse",
            chords = emptyList()
        )
    }
}

enum class Instrument(val label: String) {
    RhythmGuitar("Rhythm Guitar"),
    Voice("Voice"),
}

val amazingGrace = SongNotation(
    rootPitch = 67,
    beatsPerMeasure = 4,
    beatValue = 4,
    parts = listOf(
        SongPart(
            instrument = Instrument.RhythmGuitar,
            sections = listOf(
                SongSection(
                    title = "Verse",
                    repetitions = 3,
                    chords = listOf(
                        // G, G7, C, G
                        MeasureChord.ofNashville(1),
                        MeasureChord.ofNashville(1, extension = ChordExtension.Seventh),
                        MeasureChord.ofNashville(4),
                        MeasureChord.ofNashville(1, isPhraseEnd = true),
                        // G, Am, D, D,
                        MeasureChord.ofNashville(1),
                        MeasureChord.ofNashville(2, quality = ChordQuality.Minor),
                        MeasureChord.ofNashville(5),
                        MeasureChord.ofNashville(5, isPhraseEnd = true),
                        // G, G7, C, G
                        MeasureChord.ofNashville(1),
                        MeasureChord.ofNashville(1, extension = ChordExtension.Seventh),
                        MeasureChord.ofNashville(4),
                        MeasureChord.ofNashville(1, isPhraseEnd = true),
                        // Em, D, G, G,
                        MeasureChord.ofNashville(6, quality = ChordQuality.Minor),
                        MeasureChord.ofNashville(5),
                        MeasureChord.ofNashville(1),
                        MeasureChord.ofNashville(1, isPhraseEnd = true),
                    ),
                )
            ),
            composition = listOf(0)
        )
    ),
)

