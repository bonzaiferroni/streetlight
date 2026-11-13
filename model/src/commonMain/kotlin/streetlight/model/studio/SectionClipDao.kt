package streetlight.model.studio

import kotlinx.coroutines.flow.Flow
import streetlight.model.music.MixId
import streetlight.model.music.SectionClip
import streetlight.model.music.TrackSectionId

/** The clips bound to sections of mixes in a [StudioDb]. */
interface SectionClipDao {
    fun flowSectionClips(mixId: MixId): Flow<List<SectionClip>>

    /** Binds the clip of [sectionClip] to its section of its mix, in place of any clip bound there. */
    suspend fun saveSectionClip(sectionClip: SectionClip)
    suspend fun removeSectionClip(mixId: MixId, trackSectionId: TrackSectionId)
}
