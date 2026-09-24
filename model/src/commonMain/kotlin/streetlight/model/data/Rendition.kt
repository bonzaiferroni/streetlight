package streetlight.model.data

import kampfire.model.Labeled
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

/** A user's practice of a song, with their notes and how it went. */
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
value class RenditionId(override val value: Uuid): RecordId {
    companion object { fun random() = RenditionId(Uuid.random()) }
}

/** A rendition as it is sent. */
@Serializable
data class NewRendition(
    val songId: SongId,
    val notes: String?,
    val rating: SelfRating?,
)

/** How a user rates their own rendition. */
enum class SelfRating(override val label: String): Labeled {
    FirstSteps("First Steps"),
    NeedsWork("Needs Work"),
    ComingAlong("Coming Along"),
    Solid("Solid"),
    Banger("Banger"),
}