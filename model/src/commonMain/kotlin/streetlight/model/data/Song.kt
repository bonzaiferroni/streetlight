package streetlight.model.data

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

@Serializable
data class Song(
    val songId: SongId,
    val title: String,
    val artist: String,
)

@JvmInline @Serializable
value class SongId(override val value: Uuid): RecordId {
    companion object { fun random() = SongId(Uuid.random()) }
}