package streetlight.web.model

import kampfire.model.Messenger
import kampfire.model.ModelScope
import kampfire.model.storeOf
import kampfire.model.tapOf
import kampfire.model.toDataOr
import kotlinx.coroutines.CoroutineScope
import streetlight.model.music.Beats
import streetlight.model.music.Clip
import streetlight.model.music.ClipId
import streetlight.model.music.MusicSheet
import streetlight.model.music.MusicStudy
import streetlight.model.music.NotationId
import streetlight.model.music.Study
import streetlight.model.music.lengthOf
import streetlight.model.music.repeatsOf
import streetlight.model.music.soundingKeyOf
import streetlight.model.music.trackOf
import streetlight.model.music.ui.NotationLayout
import streetlight.model.music.ui.notationLayoutOf
import web.animations.awaitAnimationFrame

class StudySession(
    scope: CoroutineScope,
    initialStudy: MusicStudy,
    private val player: MusicPlayer,
    private val waveShelf: WaveShelf,
    private val config: MusicConfig,
    private val audio: StudioAudio,
    private val messenger: Messenger,
) {
    private val scope = ModelScope(scope, this::class)
    private val sheet = initialStudy.sheet
    private val study = initialStudy.study
    private val passes = sheet.studyPassesOf(study)
    private val state = storeOf(StudySessionState(passes.map { it.first }))
    val layoutsState = state.tapOf { it.layouts }
    val clipIdsState = state.tapOf { it.clipIds }
    val clips: Map<ClipId, Clip>
        field = mutableMapOf<ClipId, Clip>()
    val waves: Map<ClipId, Wave>
        field = mutableMapOf<ClipId, Wave>()
    val feedbacks: Map<ClipId, List<NotationFeedback>>
        field = mutableMapOf<ClipId, List<NotationFeedback>>()

    val secondsPerTick get() = sheet.music.secondsPerTick
    val isCalibratedState = audio.isCalibratedState
    val isCalibratingState = audio.isCalibratingState

    fun play() {
        scope.cancelAndLaunch("playback") {
            val musicWaves = waveShelf.musicWavesOf(sheet)
            val notationId = study.notationId
            val plan = if (notationId != null) sheet.musicPlanOf(notationId, musicWaves)
                else sheet.musicPlanOf(listOf(study.trackId), musicWaves)
            plan ?: return@cancelAndLaunch
            player.play(plan.copy(layers = plan.layers + leadLayers()))
        }
    }

    /**
     * Records a new clip over the lead-in and the click, kept in [clips] and [waves] once it reaches the end. One
     * stopped early is discarded.
     */
    fun record() {
        scope.cancelAndLaunch("playback", messenger) {
            try {
                capture()
            } finally {
                audio.recorder.cancel()
            }
        }
    }

    /** Plays the clip [clipId] from the start of its lead. */
    fun playClip(clipId: ClipId) {
        val clip = clips[clipId] ?: return
        val wave = waves[clipId] ?: return
        scope.cancelAndLaunch("playback") {
            player.play(MusicPlan(emptyList(), listOf(0 to MusicWave(wave, clip.leadSeconds)), sheet.music.secondsPerTick))
        }
    }

    fun removeClip(clipId: ClipId) {
        clips.remove(clipId)
        waves.remove(clipId)
        feedbacks.remove(clipId)
        state.update { it.copy(clipIds = it.clipIds - clipId) }
    }

    fun stop() {
        scope.cancelAndLaunch("playback") { player.stop() }
    }

    /** Measures the setup's latency, so the timing of a take is read against the beat it was sung to. */
    fun calibrate() {
        scope.cancelAndLaunch("playback") {
            player.stop()
            val outcome = audio.calibrate() ?: return@cancelAndLaunch
            outcome.toDataOr(messenger) { return@cancelAndLaunch }
            messenger.deliverSuccess("Calibrated")
        }
    }

    private suspend fun capture() {
        val music = sheet.music
        val track = sheet.trackOf(study.trackId)
        val bars = barsOf(music, track, study.notationId) ?: return
        val analyzer = MusicAnalyzer(audio.startContext().sampleRate.toInt())
        audio.recorder.start(analyzer)
        // play once the mic is open, so the lead-in is captured in full
        // td: remove once the analyzer is trusted; the study's own tones give a take that matches the notation
        val notationId = study.notationId
        val studyPlan = if (notationId != null) sheet.musicPlanOf(notationId, emptyMap())
            else sheet.musicPlanOf(listOf(study.trackId), emptyMap())
        player.play(MusicPlan(leadLayers() + studyPlan?.layers.orEmpty(), emptyList(), music.secondsPerTick))
        val downbeat = audio.clockSeconds - player.readSeconds()
        val latency = audio.latencySecondsState.now ?: 0.0
        // the last notes arrive a round trip after the study ends
        val end = downbeat + sheet.ticksOf(study) * music.secondsPerTick + latency + TAIL_SECONDS
        while (audio.clockSeconds < end) awaitAnimationFrame()

        val captured = audio.recorder.stop() ?: return
        val clip = Clip(
            clipId = ClipId.random(),
            name = "Take ${state.now.clipIds.size + 1}",
            bpm = music.bpm,
            time = music.time,
            soundingKey = music.soundingKeyOf(track),
            bars = bars,
            leadSeconds = downbeat - captured.startClock + latency,
        )
        clips[clip.clipId] = clip
        waves[clip.clipId] = captured.pcm
        feedbacks[clip.clipId] = notationFeedbackOf(
            layouts = passes.map { it.first },
            startTicks = passes.map { it.second },
            capo = (music.capo ?: 0) + (track.capo ?: 0),
            analyzer = analyzer,
            offsetSeconds = captured.startClock - latency - downbeat,
            secondsPerTick = music.secondsPerTick,
        )
        state.update { it.copy(clipIds = it.clipIds + clip.clipId) }
    }

    /** The guide note and the drummer that lead in the study, with the click while it is on. */
    private fun leadLayers(): List<ToneLayer> {
        val time = sheet.music.time
        val guide = sheet.guideMidisOf(study)?.let { guideLayerOf(time, it) }
        return listOfNotNull(guide) + drummerLayerOf(time, sheet.ticksOf(study), config.isClickOnState.now)
    }
}

data class StudySessionState(
    val layouts: List<NotationLayout>,
    val clipIds: List<ClipId> = emptyList(),
)

private const val TAIL_SECONDS = 0.1

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

/** The layout of each pass of [study] in song order, with the tick it starts at. */
private fun MusicSheet.studyPassesOf(study: Study): List<Pair<NotationLayout, Int>> {
    study.notationId?.let { notationId -> return listOfNotNull(notationLayoutOf(notationId)?.let { it to 0 }) }
    val track = trackOf(study.trackId)
    val passes = mutableMapOf<NotationId, Int>()
    var onset = 0
    return music.arrangement.flatMap { sectionId ->
        val span = music.lengthOf(sectionId) ?: Beats(0)
        val sectionStart = onset
        onset += span.ticks
        val notationId = track.sections[sectionId] ?: return@flatMap emptyList()
        val ticks = track.lengthOf(notationId)?.ticks ?: 0
        List(track.repeatsOf(notationId, span)) { repeat ->
            val passIndex = passes[notationId] ?: 0
            passes[notationId] = passIndex + 1
            notationLayoutOf(notationId, passIndex)?.let { it to sectionStart + repeat * ticks }
        }.filterNotNull()
    }
}