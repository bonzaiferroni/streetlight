package streetlight.web.model

import kampfire.model.MutableTap
import kampfire.model.Tap
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import web.animations.awaitAnimationFrame
import kotlin.math.max
import kotlin.math.min

/**
 * The state behind the studio, where samples are recorded from the microphone and played together from a cursor
 * on a timeline.
 */
class StudioEditor(
    private val scope: CoroutineScope,
    private val recorder: AudioRecorder,
    private val player: AudioPlayer,
    private val audio: StudioAudio,
) {
    private val state = storeOf(StudioEditorState())
    val sampleIdsState = state.tapOf { it.samples.map { sample -> sample.id } }
    val endSecondsState = state.tapOf { it.endSeconds }
    val spanSecondsState = state.tapOf { it.spanSeconds }
    val cursorRatioState = state.tapOf { it.cursorSeconds / it.spanSeconds }
    val isRecordingState = recorder.isRecordingState
    val isPlayingState = player.isPlayingState
    val profileState = state.tapOf { it.profile }
    val isCalibratingState = state.tapOf { it.isCalibrating }

    private val sampleMap = mutableMapOf<Int, PcmSample>()
    private val filteredMap = mutableMapOf<Int, PcmSample>()
    private var nextSampleId = 0
    private var playJob: Job? = null
    private var calibrateJob: Job? = null

    init {
        // a capture or playback left running when the studio closes would hold the microphone or keep sounding
        scope.coroutineContext[Job]?.invokeOnCompletion {
            recorder.cancel()
            player.stop()
        }
    }

    fun readSample(id: Int) = state.now.readSample(id)

    /** The filters applied to the sample of [id]. */
    fun filtersState(id: Int) = state.tapOf { state -> state.readSample(id)?.filters.orEmpty() }

    /** The PCM the sample of [id] plays, with its filters applied. */
    fun readPcm(id: Int) = filteredMap[id]

    /** Whether the sample of [id] is muted, written by a toggle. */
    fun isMutedState(id: Int) = state.mutableTapOf({ state -> state.readSample(id)?.isMuted == true }) { isMuted ->
        copy(samples = samples.map { if (it.id == id) it.copy(isMuted = isMuted) else it })
    }

    /** Whether [filter] is applied to the sample of [id], written by a toggle. */
    fun isFilteredState(id: Int, filter: AudioFilter): MutableTap<Boolean> = SettingTap(
        state.tapOf { state -> state.readSample(id)?.filters?.contains(filter) == true }
    ) { isFiltered -> setFilter(id, filter, isFiltered) }

    /** Applies [filter] to the sample of [id] when [isFiltered], or removes it, and filters its PCM again. */
    fun setFilter(id: Int, filter: AudioFilter, isFiltered: Boolean) {
        val sample = state.now.readSample(id) ?: return
        val pcm = sampleMap[id] ?: return
        val filters = if (isFiltered) sample.filters + filter else sample.filters - filter

        filteredMap[id] = pcm.applyFilters(filters)
        state.update { it.copy(samples = it.samples.map { sample -> if (sample.id == id) sample.copy(filters = filters) else sample }) }
    }

    fun record() = scope.launch {
        recorder.start()
    }

    /**
     * Stops the capture, keeping what it recorded as a sample, or stops playback when none is recording. A
     * calibration is cancelled instead.
     */
    fun stop() = scope.launch {
        if (calibrateJob?.isActive == true) {
            calibrateJob?.cancel()
            return@launch
        }

        if (!recorder.isRecordingState.now) {
            player.stop()
            return@launch
        }

        val pcm = recorder.stop()?.pcm ?: return@launch
        val id = nextSampleId++
        sampleMap[id] = pcm
        filteredMap[id] = pcm
        val sample = AudioSample(id, "Sample ${id + 1}", pcm.durationSeconds)
        state.update { it.copy(
            samples = it.samples + sample,
            spanSeconds = max(it.spanSeconds, sample.durationSeconds),
        ) }
    }

    /** Plays every sample together from the cursor, which follows playback; from the start once past the end. */
    fun play() {
        playJob?.cancel()
        playJob = scope.launch {
            val endSeconds = state.now.endSeconds ?: return@launch
            val pcms = state.now.samples.filterNot { it.isMuted }.mapNotNull { filteredMap[it.id] }
            if (state.now.cursorSeconds >= endSeconds) state.update { it.copy(cursorSeconds = 0.0) }

            val fromSeconds = state.now.cursorSeconds
            val startClock = player.play(pcms, fromSeconds)

            while (true) {
                awaitAnimationFrame()
                if (!player.isPlayingState.now) break
                val seconds = min(fromSeconds + audio.clockSeconds - startClock, endSeconds)
                state.update { it.copy(cursorSeconds = seconds) }
            }
        }
    }

    /**
     * Measures the setup's [AcousticProfile]: captures a lead-in of silence, then a sine sweep played through the
     * speakers and the tail that follows it.
     */
    fun calibrate() {
        if (recorder.isRecordingState.now || calibrateJob?.isActive == true) return
        playJob?.cancel()
        player.stop()

        calibrateJob = scope.launch {
            state.update { it.copy(isCalibrating = true) }
            try {
                recorder.start()
                delay(LEAD_IN_MILLIS)
                val sweep = sineSweepOf(audio.sampleRate)
                val sweepClock = player.play(listOf(sweep), 0.0)
                delay((sweep.durationSeconds * 1000).toLong() + TAIL_MILLIS)
                val response = recorder.stop() ?: return@launch
                val profile = measureAcousticProfile(sweep, sweepClock, response)
                state.update { it.copy(profile = profile) }
            } finally {
                recorder.cancel()
                player.stop()
                state.update { it.copy(isCalibrating = false) }
            }
        }
    }

    /** Moves the cursor to [ratio] of the timeline's span, stopping playback. */
    fun moveCursor(ratio: Double) {
        playJob?.cancel()
        player.stop()
        state.update { it.copy(cursorSeconds = ratio.coerceIn(0.0, 1.0) * it.spanSeconds) }
    }
}

data class StudioEditorState(
    val samples: List<AudioSample> = emptyList(),
    val cursorSeconds: Double = 0.0,
    val spanSeconds: Double = DEFAULT_SPAN_SECONDS,
    val profile: AcousticProfile? = null,
    val isCalibrating: Boolean = false,
) {
    /** The sample of [id], or `null` when there is none. */
    fun readSample(id: Int) = samples.firstOrNull { it.id == id }

    /** The end of playback, where the longest sample ends, or `null` without samples. */
    val endSeconds get() = samples.maxOfOrNull { it.durationSeconds }
}

private const val DEFAULT_SPAN_SECONDS = 10.0

// a lead-in long enough to measure the noise floor, and a tail long enough for the sweep to arrive and decay
private const val LEAD_IN_MILLIS = 1000L
private const val TAIL_MILLIS = 1000L

/** A toggle's state, read from [tap] and written by handing each value to [onSet]. */
private class SettingTap(
    private val tap: Tap<Boolean>,
    private val onSet: (Boolean) -> Unit,
): MutableTap<Boolean> {
    override val now get() = tap.now
    override val flow get() = tap.flow

    override fun update(transform: (Boolean) -> Boolean) = onSet(transform(now))
    override fun set(value: Boolean) = onSet(value)
    override fun set(setter: Boolean.() -> Boolean) = onSet(now.setter())
}
