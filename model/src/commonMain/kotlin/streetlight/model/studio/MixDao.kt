package streetlight.model.studio

import kotlinx.coroutines.flow.Flow
import streetlight.model.music.Mix
import streetlight.model.music.MixId
import streetlight.model.music.MusicId

/** The mixes of a [StudioDb]. */
interface MixDao {
    fun flowMixes(musicId: MusicId): Flow<List<Mix>>
    fun flowMix(mixId: MixId): Flow<Mix?>
    suspend fun saveMix(mix: Mix)

    /** Removes the mix [mixId] with its section clips. */
    suspend fun removeMix(mixId: MixId)
}
