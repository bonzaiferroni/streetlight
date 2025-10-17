package streetlight.model.data

import androidx.compose.runtime.Stable
import kabinet.utils.suggestVariation
import kotlinx.serialization.Serializable
import kotlin.collections.listOf

@Stable
@Serializable
data class SongNotation(
    val rootPitch: Int,
    val beatsPerMeasure: Int? = null,
    val measureBeats: Int = beatsPerMeasure ?: 4,
    val beatValue: Int,
    val parts: List<SongPart>,
    val composition: List<SongSection> = emptyList(),
) {
    fun validateSectionId(sectionId: String) = composition.none { it.sectionId == sectionId }

    fun suggestSectionId(rootId: String) = composition.suggestVariation(rootId) { it.sectionId }

    companion object {
        fun createEmpty(instrument: Instrument) = SongNotation(
            rootPitch = 60,
            measureBeats = 4,
            beatValue = 4,
            parts = listOf(SongPart.createEmpty(instrument)),
        )
    }
}

val amazingGrace = SongNotation(
    rootPitch = 67,
    measureBeats = 4,
    beatValue = 4,
    parts = listOf(
        SongPart(
            instrument = Instrument.RhythmGuitar,
            sequences = listOf(
                ChordSequence(
                    sequenceId = "Verse",
                    repetitions = 3,
                    chords = listOf(
                        // G, G7, C, G
                        MeasureChord.ofDegree(1),
                        MeasureChord.ofDegree(1, extension = ChordExtension.Seventh),
                        MeasureChord.ofDegree(4),
                        MeasureChord.ofDegree(1, isPhraseEnd = true),
                        // G, Am, D, D,
                        MeasureChord.ofDegree(1),
                        MeasureChord.ofDegree(2, quality = ChordQuality.Minor),
                        MeasureChord.ofDegree(5),
                        MeasureChord.ofDegree(5, isPhraseEnd = true),
                        // G, G7, C, G
                        MeasureChord.ofDegree(1),
                        MeasureChord.ofDegree(1, extension = ChordExtension.Seventh),
                        MeasureChord.ofDegree(4),
                        MeasureChord.ofDegree(1, isPhraseEnd = true),
                        // Em, D, G, G,
                        MeasureChord.ofDegree(6, quality = ChordQuality.Minor),
                        MeasureChord.ofDegree(5),
                        MeasureChord.ofDegree(1),
                        MeasureChord.ofDegree(1, isPhraseEnd = true),
                    ),
                )
            ),
        )
    ),
)

