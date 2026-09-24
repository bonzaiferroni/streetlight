package streetlight.model.data

import kotlinx.serialization.Serializable

/** A section of a song, with the layers that play in it. */
@Serializable
data class SongSection(
    val sectionId: String,
    val measures: Int,
    val layers: List<SectionLayer>,
)

/** A sequence an instrument plays in a section, from [startMeasure]. */
@Serializable
data class SectionLayer(
    val startMeasure: Int,
    val instrument: Instrument,
    val sequenceId: String,
    val repetitions: Int,
)