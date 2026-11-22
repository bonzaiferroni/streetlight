package streetlight.model.music

import kotlinx.serialization.Serializable

@Serializable
data class Study(
    val trackId: TrackId,
    val notationId: NotationId?,
    val isActive: Boolean,
)
