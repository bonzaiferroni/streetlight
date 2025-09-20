package streetlight.model.data

import kabinet.db.TableId
import kabinet.model.UserId
import kabinet.utils.randomUuidString
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Song(
    val songId: SongId,
    val userId: UserId,
    val name: String,
    val artist: String?,
    val music: String?,
)

@JvmInline @Serializable
value class SongId(override val value: String): ProjectId {
    companion object { fun random() = SongId(randomUuidString()) }
}