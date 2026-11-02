package streetlight.web.model

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
    private val recorder: AudioRecorder,
    private val player: AudioPlayer,
    private val audio: StudioAudio,
) {
    private val state = storeOf(StudioEditorState())
    val samplesState = state.tapOf { it.samples }
    val endSecondsState = state.tapOf { it.endSeconds }
    val spanSecondsState = state.tapOf { it.spanSeconds }
    val cursorRatioState = state.tapOf { it.cursorSeconds / it.spanSeconds }
    val isRecordingState = recorder.isRecordingState
    val isPlayingState = player.isPlayingState

    private val sampleMap = mutableMapOf<Int, PcmSample>()
    private var nextSampleId = 0
    private var playJob: Job? = null

    init {
        // a take or playback left running when the studio closes would hold the microphone or keep sounding
        scope.coroutineContext[Job]?.invokeOnCompletion {
            recorder.cancel()
            player.stop()
        }
    }

    fun readPcm(id: Int) = sampleMap[id]

    fun record() = scope.launch {
        recorder.start()
    }

    /** Stops the take, keeping what it recorded as a sample, or stops playback when none is recording. */
    fun stop() = scope.launch {
        if (!recorder.isRecordingState.now) {
            player.stop()
            return@launch
        }

        val pcm = recorder.stop() ?: return@launch
        val id = nextSampleId++
        sampleMap[id] = pcm
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
            val pcms = state.now.samples.mapNotNull { sampleMap[it.id] }
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
) {
    /** The end of playback, where the longest sample ends, or `null` without samples. */
    val endSeconds get() = samples.maxOfOrNull { it.durationSeconds }
}

private const val DEFAULT_SPAN_SECONDS = 10.0
