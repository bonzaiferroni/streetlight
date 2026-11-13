package streetlight.web.model

import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import streetlight.model.music.MixId
import streetlight.model.music.SectionClip
import streetlight.model.music.TrackSectionId
import streetlight.model.studio.SectionClipDao
import streetlight.web.idb.IdbTable

/** The [SectionClipDao] of a [StudioIdbDb]. */
class SectionClipIdbDao internal constructor(
    private val sectionClips: IdbTable<SectionClip>,
) : SectionClipDao {
    override fun flowSectionClips(mixId: MixId) = sectionClips.rowsFlow
        .map { rows -> rows.filter { it.mixId == mixId } }
        .distinctUntilChanged()

    override suspend fun saveSectionClip(sectionClip: SectionClip) = sectionClips.save(sectionClip)

    override suspend fun removeSectionClip(mixId: MixId, trackSectionId: TrackSectionId) =
        sectionClips.remove(keyOf(mixId, trackSectionId))
}
