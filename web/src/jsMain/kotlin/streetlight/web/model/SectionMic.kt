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
import streetlight.model.music.Music
import streetlight.model.music.SectionClip
import streetlight.model.music.SectionId
import streetlight.model.music.TrackId
import streetlight.model.music.lengthOf
import streetlight.model.music.soundingKey
import web.animations.awaitAnimationFrame
import kotlin.math.abs

/**
 * Records a take of the section [sectionId] of the track [trackId] in the mix [editor] has open: a lead-in of the
 * section's first note held a measure, when it sounds one, and two measures counted on sticks, then the section, with a click through it while [isClickOnState] is on.
 *
 * A take that reaches the end of the section is kept in [clipLibrary] under the name in
 * [nameState], and bound to the section. One stopped early is discarded. Its downbeat is placed by the round trip
 * the last calibration measured, or none before one, so it lands late by the setup's latency.
 */
class SectionMic(
    private val scope: CoroutineScope,
    private val editor: MixEditor,
    private val trackId: TrackId,
    private val sectionId: SectionId,
    private val audio: StudioAudio,
    private val tones: TonePlayer,
    private val clipLibrary: ClipLibrary,
    private val messenger: Messenger,
) {
    private val state = storeOf(SectionMicState())
    val phaseState = state.tapOf { it.phase }
    val leadProgressState = state.tapOf { it.leadProgress }
    val takeProgressState = state.tapOf { it.takeProgress }

    val nameState: MutableTap<String> = storeOf("")
    val isClickOnState: MutableTap<Boolean> = storeOf(true)

    /** Whether a calibration has measured the setup's latency. */
    val isCalibratedState = audio.isCalibratedState
    val isCalibratingState = audio.isCalibratingState

    private var job: Job? = null

    init {
        scope.launch { nameState.set(nextName()) }
    }

    /** Measures the setup's latency, so later takes land on the beat. */
    fun calibrate() {
        if (job?.isActive == true) return
        scope.launch {
            val outcome = audio.calibrate() ?: return@launch
            outcome.toDataOr(messenger) { return@launch }
            messenger.deliverSuccess("Calibrated")
        }
    }

    /** Starts a take, or stops the one under way and discards it. A take being saved runs on. */
    fun toggle() {
        if (state.now.phase == SectionMicPhase.Saving || audio.isCalibratingState.now) return
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
                state.set(SectionMicState())
            }
        }
    }

    private suspend fun record() {
        val plan = planOf() ?: return messenger.deliver(SectionMicProblem.noSection)
        try {
            audio.recorder.start()
        } catch (error: Throwable) {
            return messenger.deliver(SectionMicProblem.micFailed(error.message))
        }

        // schedule against the clock once the mic is open, so the lead-in starts in full
        val context = audio.context
        val leadStart = context.currentTime + START_DELAY_SECONDS
        val downbeat = leadStart + plan.leadSeconds
        val end = downbeat + plan.takeSeconds
        plan.guideMidis?.let { tones.playNotes(it, leadStart, plan.measureSeconds) }
        scheduleClicks(plan, leadStart + plan.guideSeconds)

        state.set(SectionMicState(SectionMicPhase.LeadIn))
        val latency = audio.latencySecondsState.now ?: 0.0
        // the last notes arrive a round trip after the section ends
        while (audio.clockSeconds < end + latency + TAIL_SECONDS) {
            val clock = audio.clockSeconds
            state.set(SectionMicState(
                phase = if (clock < downbeat) SectionMicPhase.LeadIn else SectionMicPhase.Recording,
                leadProgress = ((clock - leadStart) / plan.leadSeconds).toFloat().coerceIn(0f, 1f),
                takeProgress = ((clock - downbeat) / plan.takeSeconds).toFloat().coerceIn(0f, 1f),
            ))
            awaitAnimationFrame()
        }

        state.update { it.copy(phase = SectionMicPhase.Saving) }
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
        editor.putClip(SectionClip(trackId, sectionId, clip.clipId))
        messenger.deliverSuccess("Saved ${clip.name}")
        nameState.set(nextName())
    }

    /** The timing of a take in the open mix, or `null` while there is no mix or the track lacks the section. */
    private fun planOf(): CapturePlan? {
        val mix = editor.mixState.now ?: return null
        val track = editor.musicEditor.readTrack(trackId) ?: return null
        val length = track.lengthOf(sectionId)?.takeIf { it.ticks > 0 } ?: return null
        val music = editor.musicEditor.musicState.now
        val secondsPerTick = 60.0 / mix.bpm / music.time.pulse.ticks
        val measureTicks = music.time.measure.ticks
        // the mix may sound in another key than the music, so the guide moves the nearest way to it
        val shift = (mix.soundingKey.tonic.value - music.soundingKey.tonic.value + 6).mod(12) - 6
        val guideMidis = firstToneOf(music, track, sectionId)?.midi?.map { it + shift }
        return CapturePlan(
            music = music,
            bpm = mix.bpm,
            soundingKey = mix.soundingKeyOf(track),
            bars = barsOf(music, track, sectionId) ?: return null,
            pulseSeconds = music.time.pulse.ticks * secondsPerTick,
            pulsesPerMeasure = measureTicks / music.time.pulse.ticks,
            takeSeconds = length.ticks * secondsPerTick,
            guideMidis = guideMidis,
        )
    }

    /**
     * Counts the lead-in from [countStart] at half the pace of the pulse, then on it: the first measure on each half
     * when its pulses split in two, as "1… 2…" in 4/4, or on its downbeat alone, as in 3/4 or 6/8; the second on
     * every pulse. The click, when on, keeps every pulse through the take.
     */
    private fun scheduleClicks(plan: CapturePlan, countStart: Double) {
        val pulses = plan.pulsesPerMeasure
        val firstMeasure = if (pulses > 2 && pulses % 2 == 0) listOf(0.0, plan.measureSeconds / 2) else listOf(0.0)
        val secondMeasure = List(pulses) { plan.measureSeconds + it * plan.pulseSeconds }
        val takePulses = if (!isClickOnState.now) emptyList()
            else List(plan.bars * plan.pulsesPerMeasure) { plan.measureSeconds * 2 + it * plan.pulseSeconds }
        val measureStarts = (0..plan.bars + 1).map { it * plan.measureSeconds }
        (firstMeasure + secondMeasure + takePulses).forEach { offset ->
            val isDownbeat = measureStarts.any { abs(it - offset) < 1e-6 }
            tones.click(countStart + offset, accent = isDownbeat)
        }
    }

    /** The track and section's name with the first number from 1 that no kept clip holds. */
    private suspend fun nextName(): String {
        val base = "${trackId.name} ${sectionId.name}"
        val names = clipLibrary.readClips().mapTo(mutableSetOf()) { it.name }
        return generateSequence(1) { it + 1 }.map { "$base $it" }.first { it !in names }
    }
}

data class SectionMicState(
    val phase: SectionMicPhase = SectionMicPhase.Idle,
    val leadProgress: Float = 0f,
    val takeProgress: Float = 0f,
)

enum class SectionMicPhase { Idle, LeadIn, Recording, Saving }

object SectionMicProblem {
    val noSection = Problem("This section has nothing to record yet")
    fun micFailed(reason: String?) = Problem("The microphone could not start${reason?.let { ": $it" } ?: ""}")
}

/**
 * The timing of a take: [bars] measures of [pulseSeconds] pulses, [takeSeconds] long, after a lead-in of the
 * [guideMidis] held a measure, when the section sounds a pitch, and two measures counted.
 */
private class CapturePlan(
    val music: Music,
    val bpm: Int,
    val soundingKey: Key,
    val bars: Int,
    val pulseSeconds: Double,
    val pulsesPerMeasure: Int,
    val takeSeconds: Double,
    val guideMidis: List<Int>?,
) {
    val measureSeconds get() = pulsesPerMeasure * pulseSeconds
    val guideSeconds get() = if (guideMidis != null) measureSeconds else 0.0
    val leadSeconds get() = guideSeconds + measureSeconds * 2
}

private const val START_DELAY_SECONDS = 0.2
private const val TAIL_SECONDS = 0.1
