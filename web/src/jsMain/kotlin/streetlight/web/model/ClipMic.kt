package streetlight.web.model

import kampfire.model.Messenger
import kampfire.model.MutableTap
import kampfire.model.Problem
import kampfire.model.storeOf
import kampfire.model.tapOf
import kampfire.model.toDataOr
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import streetlight.model.music.Clip
import streetlight.model.music.ClipId
import streetlight.model.music.Key
import streetlight.model.music.Beats
import streetlight.model.music.Music
import kampfire.model.Tap
import streetlight.model.music.MusicSheet
import streetlight.model.music.NotationId
import streetlight.model.music.TrackClip
import streetlight.model.studio.StudioDb
import streetlight.model.music.TrackId
import streetlight.model.music.lengthOf
import streetlight.model.music.nameOf
import streetlight.model.music.soundingKeyOf
import web.animations.awaitAnimationFrame
import kotlin.math.abs

/**
 * Records a clip of the notation [notationId] of the track [trackId] in [sheetState], or of the whole track when
 * `null`: a lead-in of its first note held a measure, when it sounds one, and two measures counted on
 * sticks, then the notation, with a click through it while [isClickOnState] is on.
 *
 * A recording that reaches the end is kept in [clipLibrary] under the name in [nameState], and bound in [db] to the
 * notation or track. One stopped early is discarded. Its downbeat is placed by the round trip
 * the last calibration measured, or none before one, so it lands late by the setup's latency.
 */
class ClipMic(
    private val scope: CoroutineScope,
    private val sheetState: Tap<MusicSheet>,
    private val trackId: TrackId,
    private val notationId: NotationId?,
    private val audio: StudioAudio,
    private val tones: TonePlayer,
    private val clipLibrary: ClipLibrary,
    private val db: StudioDb,
    private val messenger: Messenger,
) {
    private val state = storeOf(ClipMicState())
    val phaseState = state.tapOf { it.phase }
    val leadProgressState = state.tapOf { it.leadProgress }
    val clipProgressState = state.tapOf { it.clipProgress }

    val nameState: MutableTap<String> = storeOf("")
    val isClickOnState: MutableTap<Boolean> = storeOf(true)

    /** Whether a calibration has measured the setup's latency. */
    val isCalibratedState = audio.isCalibratedState
    val isCalibratingState = audio.isCalibratingState

    private var job: Job? = null

    init {
        scope.launch { nameState.set(nextName()) }
    }

    /** Measures the setup's latency, so later clips land on the beat. */
    fun calibrate() {
        if (job?.isActive == true) return
        scope.launch {
            val outcome = audio.calibrate() ?: return@launch
            outcome.toDataOr(messenger) { return@launch }
            messenger.deliverSuccess("Calibrated")
        }
    }

    /** Starts a recording, or stops the one under way and discards it. One being saved runs on. */
    fun toggle() {
        if (state.now.phase == ClipMicPhase.Saving || audio.isCalibratingState.now) return
        if (job?.isActive == true) {
            job?.cancel()
            return
        }
        job = scope.launch {
            try {
                record()
            } finally {
                audio.recorder.cancel()
                tones.stop()
                state.set(ClipMicState())
            }
        }
    }

    private suspend fun record() {
        val plan = planOf() ?: return messenger.deliver(ClipMicProblem.nothing)
        try {
            audio.recorder.start()
        } catch (error: Throwable) {
            return messenger.deliver(ClipMicProblem.micFailed(error.message))
        }

        // schedule against the clock once the mic is open, so the lead-in starts in full
        val context = audio.context
        val leadStart = context.currentTime + START_DELAY_SECONDS
        val downbeat = leadStart + plan.leadSeconds
        val end = downbeat + plan.clipSeconds
        plan.guideMidis?.let { tones.playNotes(it, leadStart, plan.measureSeconds) }
        scheduleClicks(plan, leadStart + plan.guideSeconds)

        state.set(ClipMicState(ClipMicPhase.LeadIn))
        val latency = audio.latencySecondsState.now ?: 0.0
        // the last notes arrive a round trip after the recording ends
        while (audio.clockSeconds < end + latency + TAIL_SECONDS) {
            val clock = audio.clockSeconds
            state.set(ClipMicState(
                phase = if (clock < downbeat) ClipMicPhase.LeadIn else ClipMicPhase.Recording,
                leadProgress = ((clock - leadStart) / plan.leadSeconds).toFloat().coerceIn(0f, 1f),
                clipProgress = ((clock - downbeat) / plan.clipSeconds).toFloat().coerceIn(0f, 1f),
            ))
            awaitAnimationFrame()
        }

        state.update { it.copy(phase = ClipMicPhase.Saving) }
        val captured = audio.recorder.stop() ?: return
        val clip = Clip(
            clipId = ClipId.random(),
            name = nameState.now.trim().ifEmpty { nextName() },
            bpm = plan.bpm,
            time = plan.music.time,
            soundingKey = plan.soundingKey,
            bars = plan.bars,
            leadSeconds = downbeat - captured.startClock + latency,
        )
        clipLibrary.saveClip(clip, captured.pcm).toDataOr(messenger) { return }
        db.trackClip.saveTrackClip(TrackClip(plan.music.musicId, trackId, notationId, clip.clipId))
        messenger.deliverSuccess("Saved ${clip.name}")
        nameState.set(nextName())
    }

    /** The timing of a recording, or `null` while the track lacks the notation or it has no length. */
    private fun planOf(): CapturePlan? {
        val sheet = sheetState.now
        val track = sheet.tracks.firstOrNull { it.trackId == trackId } ?: return null
        val music = sheet.music
        val length = when (notationId) {
            null -> Beats(music.arrangement.sumOf { music.lengthOf(it)?.ticks ?: 0 })
            else -> track.lengthOf(notationId)
        }?.takeIf { it.ticks > 0 } ?: return null
        val secondsPerTick = music.secondsPerTick
        val measureTicks = music.time.measure.ticks
        // the whole track is led in by the first notation it plays
        val guideId = notationId ?: notationPlaysOf(music, track).firstOrNull()?.first
        val guideMidis = guideId?.let { firstToneOf(music, track, it) }?.midi
        return CapturePlan(
            music = music,
            bpm = music.bpm,
            soundingKey = music.soundingKeyOf(track),
            bars = barsOf(music, track, notationId) ?: return null,
            pulseSeconds = music.time.pulse.ticks * secondsPerTick,
            pulsesPerMeasure = measureTicks / music.time.pulse.ticks,
            clipSeconds = length.ticks * secondsPerTick,
            guideMidis = guideMidis,
        )
    }

    /**
     * Counts the lead-in from [countStart] at half the pace of the pulse, then on it: the first measure on each half
     * when its pulses split in two, as "1… 2…" in 4/4, or on its downbeat alone, as in 3/4 or 6/8; the second on
     * every pulse. The click, when on, keeps every pulse through the recording.
     */
    private fun scheduleClicks(plan: CapturePlan, countStart: Double) {
        val pulses = plan.pulsesPerMeasure
        val firstMeasure = if (pulses > 2 && pulses % 2 == 0) listOf(0.0, plan.measureSeconds / 2) else listOf(0.0)
        val secondMeasure = List(pulses) { plan.measureSeconds + it * plan.pulseSeconds }
        val clipPulses = if (!isClickOnState.now) emptyList()
            else List(plan.bars * plan.pulsesPerMeasure) { plan.measureSeconds * 2 + it * plan.pulseSeconds }
        val measureStarts = (0..plan.bars + 1).map { it * plan.measureSeconds }
        (firstMeasure + secondMeasure + clipPulses).forEach { offset ->
            val isDownbeat = measureStarts.any { abs(it - offset) < 1e-6 }
            tones.click(countStart + offset, accent = isDownbeat)
        }
    }

    /** The names of the track and notation with the first number from 1 that no kept clip holds. */
    private suspend fun nextName(): String {
        val sheet = sheetState.now
        val track = sheet.tracks.firstOrNull { it.trackId == trackId }
        val notationName = notationId?.let { track?.nameOf(it) }
        val base = listOfNotNull(track?.name, notationName).joinToString(" ")
        val names = clipLibrary.readClips().mapTo(mutableSetOf()) { it.name }
        return generateSequence(1) { it + 1 }.map { "$base $it" }.first { it !in names }
    }
}

data class ClipMicState(
    val phase: ClipMicPhase = ClipMicPhase.Idle,
    val leadProgress: Float = 0f,
    val clipProgress: Float = 0f,
)

enum class ClipMicPhase { Idle, LeadIn, Recording, Saving }

object ClipMicProblem {
    val nothing = Problem("This has nothing to record yet")
    fun micFailed(reason: String?) = Problem("The microphone could not start${reason?.let { ": $it" } ?: ""}")
}

/**
 * The timing of a recording: [bars] measures of [pulseSeconds] pulses, [clipSeconds] long, after a lead-in of the
 * [guideMidis] held a measure, when it sounds a pitch, and two measures counted.
 */
private class CapturePlan(
    val music: Music,
    val bpm: Int,
    val soundingKey: Key,
    val bars: Int,
    val pulseSeconds: Double,
    val pulsesPerMeasure: Int,
    val clipSeconds: Double,
    val guideMidis: List<Int>?,
) {
    val measureSeconds get() = pulsesPerMeasure * pulseSeconds
    val guideSeconds get() = if (guideMidis != null) measureSeconds else 0.0
    val leadSeconds get() = guideSeconds + measureSeconds * 2
}

private const val START_DELAY_SECONDS = 0.2
private const val TAIL_SECONDS = 0.1
