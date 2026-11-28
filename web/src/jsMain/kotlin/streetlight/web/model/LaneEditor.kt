package streetlight.web.model

import kampfire.model.Tap
import kampfire.model.combine
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import streetlight.model.music.NotationId
import streetlight.model.music.TrackId
import streetlight.model.music.ui.MeasureLine
import streetlight.model.music.ui.MusicSource
import streetlight.model.music.ui.NoteValue
import streetlight.model.music.ui.TrackSource
import streetlight.model.music.ui.measureLineOf
import streetlight.model.music.ui.musicSourceOf
import web.animations.awaitAnimationFrame
import kotlin.math.max
import kotlin.math.min

class LaneEditor(
    private val scope: CoroutineScope,
    private val host: MusicHost,
    private val waveShelf: WaveShelf,
    private val player: MusicPlayer,
) {
    private val state = storeOf(LaneEditorState())

    private val cursorStore = storeOf(0.0)

    private var playJob: Job? = null

    val windowState = state.tapOf { it.window }
    val sheetState = host.sheetState

    /** The music of every track, which the lanes are drawn from. */
    val sourceState = sheetState.tapOf { it.musicSourceOf(null, null) }

    val lanesState = sourceState.tapOf { lanesOf(it) }

    val endSecondsState: Tap<Double?> = sourceState.tapOf { endSecondsOf(it).takeIf { it > 0 } }

    val totalSecondsState: Tap<Double> = windowState.combine(endSecondsState) { window, endSeconds ->
        maxOf(window.endSeconds, endSeconds ?: 0.0, DEFAULT_WINDOW_SECONDS)
    }

    val cursorSecondsState: Tap<Double> = cursorStore

    val timeStepState = state.mutableTapOf({ it.timeStep }) { copy(timeStep = it) }

    fun play() {
        playJob?.cancel()
        playJob = scope.launch {
            val sheet = sheetState.now
            val source = sourceState.now
            val endSeconds = endSecondsOf(source).takeIf { it > 0 } ?: return@launch
            if (cursorStore.now >= endSeconds) moveCursorTo(0.0)
            val leadSeconds = leadSecondsOf(source)
            val lanes = state.now
            val playing = sheet.tracks.filter { track ->
                lanes.soloId?.let { it == track.trackId } ?: (track.trackId !in lanes.mutedTracks)
            }
            val musicWaves = waveShelf.musicWavesOf(sheet).filterKeys { it.trackId !in lanes.tonedTracks }
            val plan = source.musicPlanOf(playing.map { it.trackId }, musicWaves) ?: return@launch
            player.play(plan, fromSeconds = cursorStore.now - leadSeconds)

            while (true) {
                awaitAnimationFrame()
                if (!player.isPlayingState.now) break
                moveCursorTo((leadSeconds + player.readSeconds()).coerceIn(0.0, endSeconds))
            }
        }
    }

    fun stop() {
        playJob?.cancel()
        player.stop()
    }

    fun moveCursor(seconds: Double) {
        stop()
        moveCursorTo(seconds.coerceIn(0.0, totalSecondsState.now))
    }

    fun moveWindow(seconds: Double) {
        val lastStart = max((endSecondsState.now ?: 0.0) - windowState.now.spanSeconds, 0.0)
        state.update { it.copy(window = it.window.copy(startSeconds = min(max(seconds, 0.0), lastStart))) }
    }

    fun resizeWindow(spanSeconds: Double) {
        val totalSeconds = totalSecondsState.now
        state.update { it.copy(window = it.window.resize(spanSeconds, totalSeconds)) }
    }

    private fun moveCursorTo(seconds: Double) {
        cursorStore.set(seconds)
        val window = windowState.now.follow(seconds)
        if (window != windowState.now) state.update { it.copy(window = window) }
    }
}

data class LaneEditorState(
    val window: TimeWindow = TimeWindow(0.0, DEFAULT_WINDOW_SECONDS),
    val timeStep: NoteValue = NoteValue.Eighth,
    val isScaleSnap: Boolean = true,
    val selectedLane: Int? = null,
    val drag: NoteDrag? = null,
    val tonedTracks: Set<TrackId> = emptySet(),
    val mutedTracks: Set<TrackId> = emptySet(),
    val soloId: TrackId? = null,
)

/**
 * The lane of [track], shown as [label] and [steps] of the staff tall, holding a [LaneSection] for each time it plays
 * a notation after a lead-in [leadSeconds] long.
 */
data class Lane(
    val trackId: TrackId,
    val label: String,
    val steps: Int,
    val leadSeconds: Double,
    val track: TrackSource,
    val sections: List<LaneSection>,
)

/**
 * A section of a lane: a time its track plays a notation shown as [label] across [line], [durationSeconds] long from
 * [startSeconds]. It [isRepeat] when the lane has already played the notation.
 */
data class LaneSection(
    val label: String,
    val line: MeasureLine,
    val startSeconds: Double,
    val durationSeconds: Double,
    val isRepeat: Boolean,
)

private const val DEFAULT_WINDOW_SECONDS = 10.0
private const val LEAD_ROOM_SECONDS = 0.5

private fun endSecondsOf(source: MusicSource) = leadSecondsOf(source) + source.duration * source.secondsPerTick

private fun leadSecondsOf(source: MusicSource) = source.pickup.ticks * source.secondsPerTick + LEAD_ROOM_SECONDS

private fun lanesOf(source: MusicSource): List<Lane> {
    val leadSeconds = leadSecondsOf(source)
    return source.tracks.map { track ->
        val played = mutableSetOf<NotationId>()
        val sections = track.notations.filter { !it.isArrangement }.map { span ->
            LaneSection(
                label = span.label,
                line = span.measureLineOf(source.time.measure.ticks),
                startSeconds = leadSeconds + span.start * source.secondsPerTick,
                durationSeconds = span.ticks * source.secondsPerTick,
                isRepeat = !played.add(span.notationId),
            )
        }
        Lane(track.trackId, track.name, track.layout.window.steps, leadSeconds, track, sections)
    }
}
