package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class SongSection(
    val sectionId: String,
    val measures: Int,
    val layers: List<SectionLayer>,
)

@Serializable
data class SectionLayer(
    val startMeasure: Int,
    val instrument: Instrument,
    val sequenceId: String,
    val repetitions: Int,
)