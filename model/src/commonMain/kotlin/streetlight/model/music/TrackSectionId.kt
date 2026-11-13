package streetlight.model.music

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/** The section [sectionId] as the track [trackId] plays it. */
@Serializable
data class TrackSectionId(
    val trackId: TrackId,
    val sectionId: SectionId,
) {
    /** A name for this id that is unique among them and valid as an identifier, as a view transition wants. */
    fun toKey(prefix: String = "id") = trackId.value.toLongs { trackMost, trackLeast ->
        sectionId.value.toLongs { sectionMost, sectionLeast ->
            "$prefix-${Uuid.fromLongs(trackMost xor sectionMost, trackLeast xor sectionLeast).toHexString()}"
        }
    }
}
