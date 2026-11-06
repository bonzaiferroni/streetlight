package streetlight.web.model

import kampfire.model.Tap
import kampfire.model.combine
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import streetlight.model.data.Beats
import streetlight.model.data.MelodyTrack
import streetlight.model.data.SectionId
import streetlight.model.data.lengthOf
import streetlight.model.data.repeatsOf
import streetlight.model.data.sectionOrderOf
import web.animations.awaitAnimationFrame
import kotlin.math.max
import kotlin.math.min

/**
 * The state behind the lanes of the song in [editor]: a lane for each track on a timeline played from a cursor. The
 * timeline opens with a lead-in before the first downbeat, which holds any pickup and the first note's head.
 */
class LaneEditor(
    private val scope: CoroutineScope,
    private val editor: MusicEditor,
    private val player: TrackPlayer,
) : TimelineModel {
    private val state = storeOf(LaneEditorState())

    // the cursor moves every frame of playback, so it keeps a store of its own
    private val cursorStore = storeOf(0.0)
    private var playJob: Job? = null

    val lanesState = editor.storedState.tapOf { lanesOf(it) }
    override val endSecondsState: Tap<Double?> = editor.storedState.tapOf { endSecondsOf(it).takeIf { end -> end > 0 } }
    override val windowState = state.tapOf { it.window }
    override val totalSecondsState: Tap<Double> = windowState.combine(endSecondsState) { window, endSeconds ->
        maxOf(window.endSeconds, endSeconds ?: 0.0, DEFAULT_WINDOW_SECONDS)
    }
    override val cursorSecondsState: Tap<Double> = cursorStore

    /** The grid a dragged note's onset snaps to. */
    val timeStepState = state.mutableTapOf({ it.timeStep }) { copy(timeStep = it) }

    /** Whether a dragged note's pitch keeps to the scale, moving by semitones otherwise. */
    val isScaleSnapState = state.mutableTapOf({ it.isScaleSnap }) { copy(isScaleSnap = it) }

    /** Whether the lane at [laneIndex] is selected, which lets its notes be dragged. */
    fun isLaneSelectedState(laneIndex: Int) = state.tapOf { it.selectedLane == laneIndex }

    /** Selects the lane at [laneIndex], or clears the selection when it is already selected. */
    fun toggleLane(laneIndex: Int) = state.update {
        it.copy(selectedLane = laneIndex.takeIf { index -> index != it.selectedLane })
    }

    /** The note cursor of the lane at [laneIndex], or `null` while none of its notes is dragged. */
    fun noteCursorState(laneIndex: Int) = state.tapOf { editorState ->
        editorState.drag?.takeIf { it.laneIndex == laneIndex }?.cursor
    }

    /** Starts a drag of [note] of [slot] in the lane at [laneIndex], returning whether the lane allows one. */
    fun startNoteDrag(laneIndex: Int, slot: LaneSlot, note: NoteNotation): Boolean {
        if (state.now.selectedLane != laneIndex) return false
        val track = readMelodyTrack(laneIndex) ?: return false
        val cursor = cursorOf(note.source, slot, track)
        state.update { it.copy(drag = NoteDrag(laneIndex, slot, note.source, note.source, cursor)) }
        return true
    }

    /** Moves the note dragged to the nearest place to [tick] of its section, [stepsUp] staff steps from where it began. */
    fun dragNote(tick: Double, stepsUp: Double) {
        val drag = state.now.drag ?: return
        val track = readMelodyTrack(drag.laneIndex) ?: return
        val phrase = track.sections.firstOrNull { it.sectionId == drag.source.phraseId } ?: return
        val music = editor.storedState.now.music
        val window = staffWindowOf(track, music.key)
        val moved = drag.source
            .snapTick(tick, phrase, music, state.now.timeStep)
            .snapMidi(drag.source, stepsUp, music.key, window, state.now.isScaleSnap)
        if (moved == drag.moved) return
        state.update { it.copy(drag = drag.copy(moved = moved, cursor = cursorOf(moved, drag.slot, track))) }
        if (moved.midi != drag.moved.midi) player.playNote(moved.midi + (music.capo ?: 0) + (track.capo ?: 0))
    }

    /** Ends the drag, making its edit when [isCommitted] and the note has moved. */
    fun endNoteDrag(isCommitted: Boolean) {
        val drag = state.now.drag ?: return
        state.update { it.copy(drag = null) }
        if (!isCommitted || drag.moved == drag.source) return
        val key = editor.storedState.now.music.key
        editor.trackState(drag.laneIndex).update { track ->
            (track as? MelodyTrack)?.withMovedNote(drag.source, drag.moved, key) ?: track
        }
    }

    private fun readMelodyTrack(laneIndex: Int) = editor.readTrack(laneIndex) as? MelodyTrack

    // the cursor sits across the window, so its onset is a share of that and not of a measure
    private fun cursorOf(source: NoteSource, slot: LaneSlot, track: MelodyTrack): NoteNotation {
        val music = editor.storedState.now.music
        val seconds = slot.startSeconds + source.tick * music.secondsPerTick
        return noteNotationOf(source, windowState.now.toRatio(seconds), staffWindowOf(track, music.key), music.key)
    }

    /** Plays every track together from the cursor, which follows playback; from the start once past the end. */
    fun play() {
        playJob?.cancel()
        playJob = scope.launch {
            val endSeconds = endSecondsState.now ?: return@launch
            if (cursorStore.now >= endSeconds) moveCursorTo(0.0)
            val song = editor.storedState.now
            val leadSeconds = leadSecondsOf(song)
            player.play(song, fromSeconds = cursorStore.now - leadSeconds)

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

    override fun moveCursor(seconds: Double) {
        stop()
        moveCursorTo(seconds.coerceIn(0.0, totalSecondsState.now))
    }

    override fun moveWindow(seconds: Double) {
        val lastStart = max((endSecondsState.now ?: 0.0) - windowState.now.spanSeconds, 0.0)
        state.update { it.copy(window = it.window.copy(startSeconds = min(max(seconds, 0.0), lastStart))) }
    }

    override fun resizeWindow(spanSeconds: Double) {
        val totalSeconds = totalSecondsState.now
        state.update { it.copy(window = it.window.resize(spanSeconds, totalSeconds)) }
    }

    // move the cursor, and the window with it when the cursor leaves it
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
)

/**
 * The lane of a track, shown as [label] and [steps] of the staff tall, holding a slot for each time it plays a section
 * after a lead-in [leadSeconds] long.
 */
data class Lane(
    val label: String,
    val steps: Int,
    val leadSeconds: Double,
    val slots: List<LaneSlot>,
)

/**
 * A time a track plays the section of [notation], [durationSeconds] long from [startSeconds]. A slot [isRepeat] when
 * the lane has already played the section.
 */
data class LaneSlot(
    val notation: SectionNotation,
    val startSeconds: Double,
    val durationSeconds: Double,
    val isRepeat: Boolean,
)

/** The lanes of [song], with each track's slots laid along its arrangement as playback lays them. */
private fun lanesOf(song: StoredSong): List<Lane> {
    val tracks = song.tracks
    val secondsPerTick = song.music.secondsPerTick
    val order = tracks.sectionOrderOf(song.music)
    val leadSeconds = leadSecondsOf(song)
    return tracks.map { track ->
        val window = staffWindowOf(track, song.music.key)
        val played = mutableSetOf<SectionId>()
        var onset = 0
        val slots = order.flatMap { sectionId ->
            val span = tracks.lengthOf(sectionId) ?: Beats(0)
            val notation = sectionNotationOf(track, sectionId, song.music, window)
            val sectionSlots = if (notation == null) emptyList() else List(track.repeatsOf(sectionId, span)) { repeat ->
                LaneSlot(
                    notation = notation,
                    startSeconds = leadSeconds + (onset + repeat * notation.ticks) * secondsPerTick,
                    durationSeconds = notation.ticks * secondsPerTick,
                    isRepeat = !played.add(sectionId),
                )
            }
            onset += span.ticks
            sectionSlots
        }
        Lane(track.label, window.steps, leadSeconds, slots)
    }
}

private fun endSecondsOf(song: StoredSong): Double {
    val ticks = song.tracks.sectionOrderOf(song.music).sumOf { song.tracks.lengthOf(it)?.ticks ?: 0 }
    return leadSecondsOf(song) + ticks * song.music.secondsPerTick
}

/** The length of the lead-in of [song]: its earliest pickup, and room before it for the head of the first note. */
private fun leadSecondsOf(song: StoredSong): Double {
    val firstTick = songTonesOf(song.music, song.tracks, song.tracks).mapNotNull { it.firstTick }.minOrNull() ?: 0
    return -minOf(firstTick, 0) * song.music.secondsPerTick + LEAD_ROOM_SECONDS
}

private const val DEFAULT_WINDOW_SECONDS = 12.0
private const val LEAD_ROOM_SECONDS = 0.5
