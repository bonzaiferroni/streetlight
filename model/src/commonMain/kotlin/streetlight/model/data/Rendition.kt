package streetlight.model.data

import kampfire.model.LabeledEnum
import kampfire.model.UserId
import kampfire.utils.randomUuidString
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

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
value class RenditionId(override val value: String): ProjectId {
    companion object { fun random() = RenditionId(randomUuidString()) }
}

@Serializable
data class NewRendition(
    val songId: SongId,
    val notes: String?,
    val rating: SelfRating?,
)

enum class SelfRating(override val label: String): LabeledEnum<SelfRating> {
    FirstSteps("First Steps"),
    NeedsWork("Needs Work"),
    ComingAlong("Coming Along"),
    Solid("Solid"),
    Banger("Banger"),
}