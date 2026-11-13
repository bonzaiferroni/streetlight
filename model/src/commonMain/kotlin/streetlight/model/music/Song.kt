package streetlight.model.music

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid
import streetlight.model.data.RecordId

@Serializable
data class Song(
    val songId: SongId,
    val title: String,
    val artist: String,
)

@JvmInline @Serializable
value class SongId(override val value: Uuid): RecordId {
    override fun toString() = value.toString()
    companion object { fun random() = SongId(Uuid.random()) }
}