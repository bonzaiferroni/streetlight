package streetlight.model.data

import kabinet.model.UserId
import kabinet.utils.randomUuidString
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class SongPlay(
    val songPlayId: SongPlayId,
    val songId: SongId,
    val userId: UserId,
    val notes: String?,
    val rating: SelfRating?,
    val createdAt: Instant,
)

@JvmInline
@Serializable
value class SongPlayId(override val value: String): ProjectId {
    companion object { fun random() = SongPlayId(randomUuidString()) }
}

@Serializable
data class NewSongPlay(
    val songId: SongId,
    val notes: String?,
    val rating: SelfRating?,
)

enum class SelfRating {
    FirstSteps,
    NeedsWork,
    FeelsGood,
    Banger,
}