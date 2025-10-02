package streetlight.model.data

import kotlinx.serialization.Serializable

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