package streetlight.web.model

import kampfire.model.Ok
import kampfire.model.MutableTap
import kampfire.model.Tap
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
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
    private val audio: StudioAudio,
) : TimelineModel {
    private val state = storeOf(StudioEditorState())
    val sampleIdsState = state.tapOf { it.samples.map { sample -> sample.id } }
    override val endSecondsState = state.tapOf { it.endSeconds }
    override val totalSecondsState = state.tapOf { it.totalSeconds }
    override val windowState = state.tapOf { it.window }
    val isRecordingState = audio.recorder.isRecordingState
    val isPlayingState = audio.player.isPlayingState
    val profileState = state.tapOf { it.profile }
    val isCalibratingState = state.tapOf { it.isCalibrating }

    // the cursor moves every frame of playback and recording, so it keeps a store of its own
    private val cursorStore = storeOf(0.0)
    override val cursorSecondsState: Tap<Double> = cursorStore

    private val sampleMap = mutableMapOf<Int, PcmSample>()
    private val filteredMap = mutableMapOf<Int, PcmSample>()
    private var nextSampleId = 0
    private var captureStartSeconds = 0.0
    private var playJob: Job? = null
    private var recordJob: Job? = null
    private var calibrateJob: Job? = null

    init {
        // a capture or playback left running when the studio closes would hold the microphone or keep sounding
        scope.coroutineContext[Job]?.invokeOnCompletion {
            audio.recorder.cancel()
            audio.player.stop()
        }
    }

    fun readSample(id: Int) = state.now.readSample(id)

    /** The filters applied to the sample of [id]. */
    fun filtersState(id: Int) = state.tapOf { state -> state.readSample(id)?.filters.orEmpty() }

    /** The PCM the sample of [id] plays, with its filters applied. */
    fun readPcm(id: Int) = filteredMap[id]

    /** The peaks of the capture in progress, or `null` when none is recording. */
    fun readLivePeaks() = audio.recorder.livePeaks

    /** Where the capture in progress began on the timeline. */
    fun readLiveStartSeconds() = captureStartSeconds

    /** Where the sample of [id] begins on the timeline. */
    fun startSecondsState(id: Int) = state.tapOf { state -> state.readSample(id)?.startSeconds ?: 0.0 }

    /** Moves the sample of [id] to begin at [startSeconds], no earlier than the start of the timeline. */
    fun moveSample(id: Int, startSeconds: Double) {
        state.update { state ->
            state.copy(samples = state.samples.map {
                if (it.id == id) it.copy(startSeconds = max(startSeconds, 0.0)) else it
            })
        }
    }

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
        state.update { state ->
            state.copy(samples = state.samples.map { if (it.id == id) it.copy(filters = filters) else it })
        }
    }

    /** Starts a capture at the cursor, with the cursor following it. */
    fun record() {
        if (audio.recorder.isRecordingState.now || calibrateJob?.isActive == true) return
        playJob?.cancel()
        audio.player.stop()

        recordJob = scope.launch {
            captureStartSeconds = cursorStore.now
            audio.recorder.start()
            if (!audio.recorder.isRecordingState.now) return@launch

            while (true) {
                awaitAnimationFrame()
                val peaks = audio.recorder.livePeaks ?: break
                moveCursorTo(captureStartSeconds + peaks.durationSeconds)
            }
        }
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

        if (!audio.recorder.isRecordingState.now) {
            playJob?.cancel()
            audio.player.stop()
            return@launch
        }

        recordJob?.cancel()
        val pcm = audio.recorder.stop()?.pcm ?: return@launch
        val id = nextSampleId++
        sampleMap[id] = pcm
        filteredMap[id] = pcm
        val sample = AudioSample(id, "Sample ${id + 1}", pcm.durationSeconds, captureStartSeconds)
        state.update { it.copy(samples = it.samples + sample) }
    }

    /** Plays every sample together from the cursor, which follows playback; from the start once past the end. */
    fun play() {
        if (audio.recorder.isRecordingState.now) return
        playJob?.cancel()
        playJob = scope.launch {
            val endSeconds = state.now.endSeconds ?: return@launch
            val placements = state.now.samples
                .filterNot { it.isMuted }
                .mapNotNull { sample -> filteredMap[sample.id]?.let { PlacedPcm(it, sample.startSeconds) } }
            if (cursorStore.now >= endSeconds) moveCursorTo(0.0)

            val fromSeconds = cursorStore.now
            val startClock = audio.player.play(placements, fromSeconds)

            while (true) {
                awaitAnimationFrame()
                if (!audio.player.isPlayingState.now) break
                moveCursorTo(min(fromSeconds + audio.clockSeconds - startClock, endSeconds))
            }
        }
    }

    /** Measures the setup's [AcousticProfile] through [StudioAudio.calibrate]. */
    fun calibrate() {
        if (audio.recorder.isRecordingState.now || calibrateJob?.isActive == true) return
        playJob?.cancel()
        audio.player.stop()

        calibrateJob = scope.launch {
            state.update { it.copy(isCalibrating = true) }
            try {
                val profile = (audio.calibrate() as? Ok)?.data ?: return@launch
                state.update { it.copy(profile = profile) }
            } finally {
                state.update { it.copy(isCalibrating = false) }
            }
        }
    }

    override fun moveCursor(seconds: Double) {
        if (audio.recorder.isRecordingState.now) return
        playJob?.cancel()
        audio.player.stop()
        moveCursorTo(seconds.coerceIn(0.0, state.now.totalSeconds))
    }

    override fun moveWindow(seconds: Double) {
        state.update { state ->
            val window = state.window
            val lastStart = max((state.endSeconds ?: 0.0) - window.spanSeconds, 0.0)
            state.copy(window = window.copy(startSeconds = seconds.coerceIn(0.0, lastStart)))
        }
    }

    override fun resizeWindow(spanSeconds: Double) {
        state.update { it.copy(window = it.window.resize(spanSeconds, it.totalSeconds)) }
    }

    // move the cursor, and the window with it when the cursor leaves it
    private fun moveCursorTo(seconds: Double) {
        cursorStore.set(seconds)
        val window = state.now.window.follow(seconds)
        if (window != state.now.window) state.update { it.copy(window = window) }
    }
}

data class StudioEditorState(
    val samples: List<AudioSample> = emptyList(),
    val window: TimeWindow = TimeWindow(0.0, DEFAULT_WINDOW_SECONDS),
    val profile: AcousticProfile? = null,
    val isCalibrating: Boolean = false,
) {
    /** The sample of [id], or `null` when there is none. */
    fun readSample(id: Int) = samples.firstOrNull { it.id == id }

    /** The end of playback, where the last sample ends, or `null` without samples. */
    val endSeconds get() = samples.maxOfOrNull { it.endSeconds }

    /** The length of the whole timeline, through the samples and the window. */
    val totalSeconds get() = maxOf(window.endSeconds, endSeconds ?: 0.0, DEFAULT_WINDOW_SECONDS)
}

private const val DEFAULT_WINDOW_SECONDS = 10.0

// a lead-in long enough to measure the noise floor, and a tail long enough for the sweep to arrive and decay

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
