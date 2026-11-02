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
    val sampleIdsState = state.tapOf { it.sampleIds }
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

    fun readSample(id: Int) = sampleMap[id]

    fun record() = scope.launch {
        recorder.start()
    }

    /** Stops the take, keeping what it recorded as a sample, or stops playback when none is recording. */
    fun stop() = scope.launch {
        if (!recorder.isRecordingState.now) {
            player.stop()
            return@launch
        }

        val sample = recorder.stop() ?: return@launch
        val id = nextSampleId++
        sampleMap[id] = sample
        state.update { it.copy(
            sampleIds = it.sampleIds + id,
            spanSeconds = max(it.spanSeconds, sample.durationSeconds),
        ) }
    }

    /** Plays every sample together from the cursor, which follows playback; from the start once past the end. */
    fun play() {
        playJob?.cancel()
        playJob = scope.launch {
            val samples = state.now.sampleIds.mapNotNull { sampleMap[it] }
            val endSeconds = samples.maxOfOrNull { it.durationSeconds } ?: return@launch
            if (state.now.cursorSeconds >= endSeconds) state.update { it.copy(cursorSeconds = 0.0) }

            val fromSeconds = state.now.cursorSeconds
            val startClock = player.play(samples, fromSeconds)

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
    val sampleIds: List<Int> = emptyList(),
    val cursorSeconds: Double = 0.0,
    val spanSeconds: Double = DEFAULT_SPAN_SECONDS,
)

private const val DEFAULT_SPAN_SECONDS = 10.0
