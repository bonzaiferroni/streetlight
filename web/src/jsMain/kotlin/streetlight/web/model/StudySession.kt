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
import streetlight.model.music.lengthOf
import streetlight.model.music.repeatsOf
import streetlight.model.music.trackOf
import streetlight.model.music.ui.NotationLayout
import streetlight.model.music.ui.notationLayoutOf

class StudySession(
    scope: CoroutineScope,
    initialStudy: MusicStudy,
    private val player: MusicPlayer,
    private val waveShelf: WaveShelf,
    private val config: MusicConfig,
) {
    private val scope = ModelScope(scope, this::class)
    private val sheet = initialStudy.sheet
    private val study = initialStudy.study
    private val state = storeOf(StudySessionState(sheet.notationLayoutsOf(study)))
    private val studyTicks = sheet.ticksOf(study)
    private val guideMidis = sheet.guideMidisOf(study)

    val layoutsState = state.tapOf { it.layouts }

    fun play() {
        scope.cancelAndLaunch("playback") {
            val musicWaves = waveShelf.musicWavesOf(sheet)
            val notationId = study.notationId
            val plan = if (notationId != null) sheet.musicPlanOf(notationId, musicWaves)
                else sheet.musicPlanOf(listOf(study.trackId), musicWaves)
            plan ?: return@cancelAndLaunch
            player.play(plan.withLeadIn(sheet.music.time, studyTicks, config.isClickOnState.now, guideMidis))
        }
    }

    fun stop() {
        scope.cancelAndLaunch("playback") { player.stop() }
    }
}

data class StudySessionState(
    val layouts: List<NotationLayout>
)

/** The ticks [study] lasts: its notation's length, or the whole arrangement for a whole track. */
private fun MusicSheet.ticksOf(study: Study) = when (val notationId = study.notationId) {
    null -> music.arrangement.sumOf { music.lengthOf(it)?.ticks ?: 0 }
    else -> trackOf(study.trackId).lengthOf(notationId)?.ticks ?: 0
}

/** The pitches of the first tone of [study], led by the first notation its track plays for a whole track. */
private fun MusicSheet.guideMidisOf(study: Study): List<Int>? {
    val track = trackOf(study.trackId)
    val guideId = study.notationId ?: notationPassesOf(music, track).firstOrNull()?.first
    return guideId?.let { firstToneOf(music, track, it) }?.midi
}

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