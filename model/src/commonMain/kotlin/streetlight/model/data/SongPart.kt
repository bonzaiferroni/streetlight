package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class SongPart(
    val instrument: Instrument,
    val style: NotationStyle = NotationStyle.Letters,
    val midiProgram: Int? = null,
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

enum class Instrument(val label: String, val midiProgram: Int) {
    RhythmGuitar("Rhythm Guitar", 24),
    Voice("Voice", 52),
}