package streetlight.model.music

import kotlinx.serialization.Serializable

/** Binds the clip [clipId] to the section [sectionId] of the track [trackId]. */
@Serializable
data class SectionClip(
    val trackId: TrackId,
    val sectionId: SectionId,
    val clipId: ClipId,
)
