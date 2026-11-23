package streetlight.web.model

import kampfire.model.ModelScope
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import streetlight.model.music.Beats
import streetlight.model.music.MusicSheet
import streetlight.model.music.MusicStudy
import streetlight.model.music.NotationId
import streetlight.model.music.Study
import streetlight.model.music.repeatsOf
import streetlight.model.music.trackOf
import streetlight.model.music.ui.NotationLayout
import streetlight.model.music.ui.notationLayoutOf

class StudySession(
    scope: CoroutineScope,
    initialStudy: MusicStudy,
) {
    private val scope = ModelScope(scope, this::class)
    private val sheet = initialStudy.sheet
    private val state = storeOf(StudySessionState(sheet.notationLayoutsOf(initialStudy.study)))

    val layoutsState = state.tapOf { it.layouts }
}

data class StudySessionState(
    val layouts: List<NotationLayout>
)

private fun MusicSheet.notationLayoutsOf(study: Study): List<NotationLayout> {
    study.notationId?.let { return listOfNotNull(notationLayoutOf(it)) }
    val track = trackOf(study.trackId)
    val passes = mutableMapOf<NotationId, Int>()
    return music.arrangement.flatMap { sectionId ->
        val notationId = track.sections[sectionId] ?: return@flatMap emptyList()
        val span = music.lengthOf(sectionId) ?: Beats(0)
        List(track.repeatsOf(notationId, span)) {
            val passIndex = passes[notationId] ?: 0
            passes[notationId] = passIndex + 1
            notationLayoutOf(notationId, passIndex)
        }.filterNotNull()
    }
}