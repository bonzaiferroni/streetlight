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
    private val player: MusicPlayer,
    private val waveShelf: WaveShelf,
) {
    private val scope = ModelScope(scope, this::class)
    private val sheet = initialStudy.sheet
    private val study = initialStudy.study
    private val state = storeOf(StudySessionState(sheet.notationLayoutsOf(study)))

    val layoutsState = state.tapOf { it.layouts }

    fun play() {
        scope.cancelAndLaunch("playback") {
            val musicWaves = waveShelf.musicWavesOf(sheet)
            val plan = study.notationId?.let { sheet.musicPlanOf(it, musicWaves) }
                ?: sheet.musicPlanOf(listOf(study.trackId), musicWaves)
            plan?.let { player.play(it) }
        }
    }

    fun stop() {
        scope.cancelAndLaunch("playback") { player.stop() }
    }
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