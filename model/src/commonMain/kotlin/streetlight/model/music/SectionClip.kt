package streetlight.model.music

import kotlinx.serialization.Serializable

/** The section [sectionId] as the track [trackId] plays it. */
@Serializable
data class TrackSection(
    val trackId: TrackId,
    val sectionId: SectionId,
)

/** Binds the clip [clipId] to the section [sectionId] of the track [trackId]. */
@Serializable
data class SectionClip(
    val trackId: TrackId,
    val sectionId: SectionId,
    val clipId: ClipId,
)
