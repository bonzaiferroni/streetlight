package streetlight.web.model

import kampfire.model.Tap
import kampfire.model.combine
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import streetlight.model.music.Beats
import streetlight.model.music.MusicSheet
import streetlight.model.music.NotationId
import streetlight.model.music.Track
import streetlight.model.music.TrackId
import streetlight.model.music.lengthOf
import streetlight.model.music.repeatsOf
import streetlight.model.music.ui.NoteValue
import streetlight.model.music.ui.StaffNotation
import streetlight.model.music.ui.StaffWindow
import streetlight.model.music.ui.drawnLyricLinesOf
import streetlight.model.music.ui.staffNotationOf
import streetlight.model.music.ui.staffWindowOf
import web.animations.awaitAnimationFrame
import kotlin.math.max
import kotlin.math.min

class LaneEditor(
    private val scope: CoroutineScope,
    private val sheetState: Tap<MusicSheet>,
    private val waveShelf: WaveShelf,
    private val player: TrackPlayer,
) {
    private val state = storeOf(LaneEditorState())

    private val cursorStore = storeOf(0.0)

    private var playJob: Job? = null

    val windowState = state.tapOf { it.window }

    private val rateState = sheetState.tapOf { it.music.secondsPerTick }

    val lanesState = sheetState.combine(rateState) { sheet, rate ->
        lanesOf(sheet, rate)
    }

    val endSecondsState: Tap<Double?> = sheetState.combine(rateState,) { sheet, perTick ->
        endSecondsOf(sheet, perTick).takeIf { it > 0 }
    }

    val totalSecondsState: Tap<Double> = windowState.combine(endSecondsState) { window, endSeconds ->
        maxOf(window.endSeconds, endSeconds ?: 0.0, DEFAULT_WINDOW_SECONDS)
    }

    val cursorSecondsState: Tap<Double> = cursorStore

    val timeStepState = state.mutableTapOf({ it.timeStep }) { copy(timeStep = it) }

    fun play() {
        playJob?.cancel()
        playJob = scope.launch {
            val sheet = sheetState.now
            val rate = rateState.now
            val endSeconds = endSecondsOf(sheet, rate).takeIf { it > 0 } ?: return@launch
            if (cursorStore.now >= endSeconds) moveCursorTo(0.0)
            val leadSeconds = leadSecondsOf(sheet, rate)
            val lanes = state.now
            val playing = sheet.tracks.filter { track ->
                lanes.soloId?.let { it == track.trackId } ?: (track.trackId !in lanes.mutedTracks)
            }
            val musicWaves = waveShelf.musicWavesOf(sheet).filterKeys { it.trackId !in lanes.tonedTracks }
            player.play(sheet, playing, fromSeconds = cursorStore.now - leadSeconds, musicWaves = musicWaves)

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
 * The lane of a track, shown as [label] and [steps] of the staff tall, holding a [LaneSection] for each time it plays
 * a notation after a lead-in [leadSeconds] long.
 */
data class Lane(
    val trackId: TrackId,
    val label: String,
    val steps: Int,
    val leadSeconds: Double,
    val sections: List<LaneSection>,
)

/**
 * A section of a lane: a time its track plays [notation], [durationSeconds] long from [startSeconds]. It [isRepeat]
 * when the lane has already played the notation.
 */
data class LaneSection(
    val notation: StaffNotation,
    val startSeconds: Double,
    val durationSeconds: Double,
    val isRepeat: Boolean,
)

private const val DEFAULT_WINDOW_SECONDS = 10.0
private const val LEAD_ROOM_SECONDS = 0.5

private fun endSecondsOf(sheet: MusicSheet, secondsPerTick: Double): Double {
    val ticks = sheet.music.arrangement.sumOf { sheet.music.lengthOf(it)?.ticks ?: 0 }
    return leadSecondsOf(sheet, secondsPerTick) + ticks * secondsPerTick
}

private fun leadSecondsOf(sheet: MusicSheet, secondsPerTick: Double): Double {
    val firstTick = songTonesOf(sheet.music, sheet.tracks).mapNotNull { it.firstTick }.minOrNull() ?: 0
    return -minOf(firstTick, 0) * secondsPerTick + LEAD_ROOM_SECONDS
}

private fun lanesOf(sheet: MusicSheet, rate: Double): List<Lane> {
    val tracks = sheet.tracks
    val music = sheet.music
    val leadSeconds = leadSecondsOf(sheet, rate)
    return tracks.map { track ->
        val window = laneWindowOf(track, sheet)
        val plays = mutableMapOf<NotationId, Int>()
        var onset = 0
        val sections = music.arrangement.flatMap { sectionId ->
            val span = music.lengthOf(sectionId) ?: Beats(0)
            val notationId = track.sections[sectionId]
            val ticks = notationId?.let { track.lengthOf(it) }?.ticks ?: 0
            val laneSections = if (notationId == null) emptyList() else List(track.repeatsOf(notationId, span)) { repeat ->
                val play = plays[notationId] ?: 0
                val lyrics = drawnLyricLinesOf(tracks, track, notationId, music, play)
                val notation = staffNotationOf(track, notationId, music, window, lyrics) ?: return@List null
                plays[notationId] = play + 1
                LaneSection(
                    notation = notation,
                    startSeconds = leadSeconds + (onset + repeat * ticks) * rate,
                    durationSeconds = ticks * rate,
                    isRepeat = play > 0,
                )
            }.filterNotNull()
            onset += span.ticks
            laneSections
        }
        Lane(track.trackId, track.name, window.steps, leadSeconds, sections)
    }
}

fun laneWindowOf(track: Track, sheet: MusicSheet): StaffWindow {
    return staffWindowOf(track, sheet.music.key)
}