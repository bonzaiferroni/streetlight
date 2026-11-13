package streetlight.model.studio

import kotlinx.coroutines.flow.Flow
import streetlight.model.music.Clip
import streetlight.model.music.ClipId

/** The clips of a [StudioDb], without their audio. */
interface ClipDao {
    fun flowClips(): Flow<List<Clip>>
    fun flowClip(clipId: ClipId): Flow<Clip?>
    suspend fun saveClip(clip: Clip)

    /** Removes the clip [clipId] and each binding of it to a section. */
    suspend fun removeClip(clipId: ClipId)
}
