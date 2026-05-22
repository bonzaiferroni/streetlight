package streetlight.model.data

import kampfire.model.Labeled
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

@Serializable
data class Rendition(
    val renditionId: RenditionId,
    val songId: SongId,
    val starId: StarId,
    val notes: String?,
    val rating: SelfRating?,
    val createdAt: Instant,
)

@JvmInline
@Serializable
value class RenditionId(override val value: Uuid): ProjectId {
    companion object { fun random() = RenditionId(Uuid.random()) }
}

@Serializable
data class NewRendition(
    val songId: SongId,
    val notes: String?,
    val rating: SelfRating?,
)

enum class SelfRating(override val label: String): Labeled<SelfRating> {
    FirstSteps("First Steps"),
    NeedsWork("Needs Work"),
    ComingAlong("Coming Along"),
    Solid("Solid"),
    Banger("Banger"),
}