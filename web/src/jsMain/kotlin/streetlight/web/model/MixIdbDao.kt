package streetlight.web.model

import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import streetlight.model.music.Mix
import streetlight.model.music.MixId
import streetlight.model.music.MusicId
import streetlight.model.studio.MixDao
import streetlight.web.idb.IdbTable

/** The [MixDao] of a [StudioIdbDb]. */
class MixIdbDao internal constructor(
    private val mixes: IdbTable<Mix>,
) : MixDao {
    override fun flowMixes(musicId: MusicId) = mixes.rowsFlow
        .map { rows -> rows.filter { it.musicId == musicId } }
        .distinctUntilChanged()

    override fun flowMix(mixId: MixId) = mixes.flowRow(mixId.toString())
    override suspend fun saveMix(mix: Mix) = mixes.save(mix)
    override suspend fun removeMix(mixId: MixId) = mixes.remove(mixId.toString())
}
