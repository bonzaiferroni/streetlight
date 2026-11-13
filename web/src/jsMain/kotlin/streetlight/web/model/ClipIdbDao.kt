package streetlight.web.model

import streetlight.model.music.Clip
import streetlight.model.music.ClipId
import streetlight.model.studio.ClipDao
import streetlight.web.idb.IdbTable

/** The [ClipDao] of a [StudioIdbDb]. */
class ClipIdbDao internal constructor(
    private val clips: IdbTable<Clip>,
) : ClipDao {
    override fun flowClips() = clips.rowsFlow
    override fun flowClip(clipId: ClipId) = clips.flowRow(clipId.toString())
    override suspend fun saveClip(clip: Clip) = clips.save(clip)
    override suspend fun removeClip(clipId: ClipId) = clips.remove(clipId.toString())
}
