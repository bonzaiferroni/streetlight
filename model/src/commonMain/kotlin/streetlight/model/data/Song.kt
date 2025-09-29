package streetlight.model.data

import kabinet.model.UserId
import kabinet.utils.randomUuidString
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Song(
    val songId: SongId,
    val userId: UserId,
    val title: String,
    val artist: String?,
    val tempo: Int?,
    val capo: Int?,
    val notation: SongNotation?,
    val updatedAt: Instant,
    val createdAt: Instant,
)

@JvmInline @Serializable
value class SongId(override val value: String): ProjectId {
    companion object { fun random() = SongId(randomUuidString()) }
}