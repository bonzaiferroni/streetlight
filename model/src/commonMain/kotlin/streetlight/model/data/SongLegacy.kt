package streetlight.model.data

import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid
import streetlight.model.music.SongId

/** A song in a user's repertoire, with its notation. */
@Serializable
data class SongLegacy(
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

/** A song at an event, with its request if there is one. */
@Serializable
data class EventSong(
    val song: SongLegacy,
    val request: Request?,
)