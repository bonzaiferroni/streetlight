package streetlight.model.data

import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

/** A song in a user's repertoire, with its notation. */
@Serializable
data class Song(
    val songId: SongId,
    val starId: StarId,
    val title: String,
    val artist: String,
    val tempo: Int?,
    val capo: Int?,
    val notation: SongNotation?,
    val inRotation: Boolean,
    val updatedAt: Instant,
    val createdAt: Instant,
)

@JvmInline @Serializable
value class SongId(override val value: Uuid): RecordId {
    companion object { fun random() = SongId(Uuid.random()) }
}

/** A song at an event, with its request if there is one. */
@Serializable
data class EventSong(
    val song: Song,
    val request: Request?,
)