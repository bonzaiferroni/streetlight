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
import streetlight.model.music.ChordTrack
import streetlight.model.music.MelodyTrack
import streetlight.model.music.SectionId
import streetlight.model.music.Track
import streetlight.model.music.TrackId
import streetlight.model.music.lengthOf
import streetlight.model.music.lyricsOf
import streetlight.model.music.repeatsOf
import streetlight.model.music.sectionOrderOf
import streetlight.model.music.toMidi
import streetlight.model.music.ui.NoteNotation
import streetlight.model.music.ui.NoteSource
import streetlight.model.music.ui.NoteValue
import streetlight.model.music.ui.SectionNotation
import streetlight.model.music.ui.StaffWindow
import streetlight.model.music.ui.drawnLyricLinesOf
import streetlight.model.music.ui.noteNotationOf
import streetlight.model.music.ui.sectionNotationOf
import streetlight.model.music.ui.staffWindowOf
import web.animations.awaitAnimationFrame
import kotlin.math.max
import kotlin.math.min

/**
 * The state behind the lanes of the song in [editor], at the tempo and with the takes of [mix]: a lane for each track on
 * a timeline played from a cursor. The
 * timeline opens with a lead-in before the first downbeat, which holds any pickup and the first note's head.
 */
class LaneEditor(
    private val scope: CoroutineScope,
    private val editor: MusicEditor,
    private val mix: MixEditor,
    private val player: TrackPlayer,
    private val tones: TonePlayer,
) : TimelineModel {
    private val state = storeOf(LaneEditorState())

    // the cursor moves every frame of playback, so it keeps a store of its own
    private val cursorStore = storeOf(0.0)
    private var playJob: Job? = null

    // laid out at the tempo playback runs at, so the cursor keeps with what sounds
    private val layoutState = mix.playbackSecondsPerTickState.combine(editor.isLyricsShownState) { perTick, isLyricsShown ->
        perTick to isLyricsShown
    }
    val lanesState = editor.storedState.combine(layoutState) { song, (perTick, isLyricsShown) ->
        lanesOf(song, perTick, isLyricsShown)
    }
    override val endSecondsState: Tap<Double?> = editor.storedState.combine(
        mix.playbackSecondsPerTickState,
    ) { song, perTick ->
        endSecondsOf(song, perTick).takeIf { it > 0 }
    }
    override val windowState = state.tapOf { it.window }
    override val totalSecondsState: Tap<Double> = windowState.combine(endSecondsState) { window, endSeconds ->
        maxOf(window.endSeconds, endSeconds ?: 0.0, DEFAULT_WINDOW_SECONDS)
    }
    override val cursorSecondsState: Tap<Double> = cursorStore

    /** The grid a dragged note's onset snaps to. */
    val timeStepState = state.mutableTapOf({ it.timeStep }) { copy(timeStep = it) }

    /** Whether a dragged note's pitch keeps to the scale, moving by semitones otherwise. */
    val isScaleSnapState = state.mutableTapOf({ it.isScaleSnap }) { copy(isScaleSnap = it) }

    /** Whether a melody's lane shows the lyrics sung to it. */
    val isLyricsShownState = editor.isLyricsShownState

    /** Whether the lane at [laneIndex] is selected, which lets its notes be dragged. */
    fun isLaneSelectedState(laneIndex: Int) = state.tapOf { it.selectedLane == laneIndex }

    /** Whether the track [trackId] plays its tones in place of its takes. */
    fun isTonedState(trackId: TrackId) = state.tapOf { trackId in it.tonedTracks }

    /** Whether the track [trackId] is silent in playback. */
    fun isMutedState(trackId: TrackId) = state.tapOf { trackId in it.mutedTracks }

    /** Whether the track [trackId] plays alone. */
    fun isSoloedState(trackId: TrackId) = state.tapOf { it.soloId == trackId }

    /** Plays the tones of the track [trackId] in place of its takes, or its takes again. */
    fun toggleTones(trackId: TrackId) = state.update { it.copy(tonedTracks = it.tonedTracks.toggled(trackId)) }

    /** Silences the track [trackId] in playback, or sounds it again. */
    fun toggleMute(trackId: TrackId) = state.update { it.copy(mutedTracks = it.mutedTracks.toggled(trackId)) }

    /** Plays the track [trackId] alone, muted or not, or every track again when it already plays alone. */
    fun toggleSolo(trackId: TrackId) = state.update { it.copy(soloId = trackId.takeIf { id -> id != it.soloId }) }

    /** Selects the lane at [laneIndex], or clears the selection when it is already selected. */
    fun toggleLane(laneIndex: Int) = state.update {
        it.copy(selectedLane = laneIndex.takeIf { index -> index != it.selectedLane })
    }

    /** The note cursor of the lane at [laneIndex], or `null` while none of its notes is dragged. */
    fun noteCursorState(laneIndex: Int) = state.tapOf { editorState ->
        editorState.drag?.takeIf { it.laneIndex == laneIndex }?.cursor
    }

    /** Starts a drag of [note] of [section] in the lane at [laneIndex], returning whether the lane allows one. */
    fun startNoteDrag(laneIndex: Int, section: LaneSection, note: NoteNotation): Boolean {
        if (state.now.selectedLane != laneIndex) return false
        val track = readNotedTrack(laneIndex) ?: return false
        val source = note.source ?: return false
        val cursor = cursorOf(source, section, track)
        state.update { it.copy(drag = NoteDrag(laneIndex, section, source, source, cursor)) }
        return true
    }

    /** Moves the note dragged to the nearest place to [tick] of its section, [stepsUp] staff steps from where it began. */
    fun dragNote(tick: Double, stepsUp: Double) {
        val drag = state.now.drag ?: return
        val track = readNotedTrack(drag.laneIndex) ?: return
        val music = editor.storedState.now.music
        val window = laneWindowOf(track, editor.storedState.now, editor.isLyricsShownState.now)
        val moved = drag.source
            .snapTick(tick, track, music, state.now.timeStep)
            .snapPitch(drag.source, stepsUp, music.key, window, state.now.isScaleSnap)
        if (moved == drag.moved) return
        state.update { it.copy(drag = drag.copy(moved = moved, cursor = cursorOf(moved, drag.section, track))) }
        if (moved.midi != drag.moved.midi) {
            val capo = (music.capo ?: 0) + (track.capo ?: 0)
            val midis = moved.chord?.toMidi(music.key) ?: listOf(moved.midi)
            tones.playNotes(midis.map { it + capo })
        }
    }

    /** Ends the drag, making its edit when [isCommitted] and the note has moved. */
    fun endNoteDrag(isCommitted: Boolean) {
        val drag = state.now.drag ?: return
        state.update { it.copy(drag = null) }
        if (!isCommitted || drag.moved == drag.source) return
        val key = editor.storedState.now.music.key
        editor.trackState(drag.laneIndex).update { track ->
            track?.withMovedNote(drag.source, drag.moved, key)
        }
    }

    // the tracks whose notes can be dragged
    private fun readNotedTrack(laneIndex: Int) =
        editor.readTrack(laneIndex)?.takeIf { it is MelodyTrack || it is ChordTrack }

    // the cursor sits across the window, so its onset is a share of that and not of a measure
    private fun cursorOf(source: NoteSource, section: LaneSection, track: Track): NoteNotation {
        val song = editor.storedState.now
        val seconds = section.startSeconds + source.tick * mix.playbackSecondsPerTickState.now
        val window = laneWindowOf(track, song, editor.isLyricsShownState.now)
        return noteNotationOf(source, windowState.now.toRatio(seconds), window, song.music.key)
    }

    /**
     * Plays the tracks together from the cursor, which follows playback; from the start once past the end. A soloed
     * track plays alone, muted tracks drop out, and a track playing its tones leaves its takes out.
     */
    fun play() {
        playJob?.cancel()
        playJob = scope.launch {
            val endSeconds = endSecondsState.now ?: return@launch
            if (cursorStore.now >= endSeconds) moveCursorTo(0.0)
            val song = editor.storedState.now
            val leadSeconds = leadSecondsOf(song, mix.playbackSecondsPerTickState.now)
            val lanes = state.now
            val playing = song.tracks.filter { track ->
                lanes.soloId?.let { it == track.trackId } ?: (track.trackId !in lanes.mutedTracks)
            }
            val takes = mix.readTakes()?.let { takes ->
                SongTakes(takes.bpm, takes.takes.filterKeys { it.trackId !in lanes.tonedTracks })
            }
            player.play(song, playing, fromSeconds = cursorStore.now - leadSeconds, takes = takes)

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
    val tonedTracks: Set<TrackId> = emptySet(),
    val mutedTracks: Set<TrackId> = emptySet(),
    val soloId: TrackId? = null,
)

/**
 * The lane of a track, shown as [label] and [steps] of the staff tall, holding a [LaneSection] for each time it plays
 * one after a lead-in [leadSeconds] long.
 */
data class Lane(
    val trackId: TrackId,
    val label: String,
    val steps: Int,
    val leadSeconds: Double,
    val sections: List<LaneSection>,
)

/**
 * A section of a lane: a time its track plays the section of [notation], [durationSeconds] long from [startSeconds]. It
 * [isRepeat] when the lane has already played the section.
 */
data class LaneSection(
    val notation: SectionNotation,
    val startSeconds: Double,
    val durationSeconds: Double,
    val isRepeat: Boolean,
)

/**
 * The lanes of [song], with each track's sections laid along its arrangement as playback lays them. With
 * [isLyricsShown], each time a track plays a section carries the line of lyrics sung to it then.
 */
private fun lanesOf(song: StoredSong, secondsPerTick: Double, isLyricsShown: Boolean): List<Lane> {
    val tracks = song.tracks
    val order = tracks.sectionOrderOf(song.music)
    val leadSeconds = leadSecondsOf(song, secondsPerTick)
    return tracks.map { track ->
        val window = laneWindowOf(track, song, isLyricsShown)
        val plays = mutableMapOf<SectionId, Int>()
        var onset = 0
        val sections = order.flatMap { sectionId ->
            val span = tracks.lengthOf(sectionId) ?: Beats(0)
            val ticks = track.lengthOf(sectionId)?.ticks ?: 0
            val laneSections = List(track.repeatsOf(sectionId, span)) { repeat ->
                val play = plays[sectionId] ?: 0
                val lyrics = if (window.lyricLines == 0) emptyList()
                else drawnLyricLinesOf(tracks, track, sectionId, song.music, play)
                val notation = sectionNotationOf(track, sectionId, song.music, window, lyrics) ?: return@List null
                plays[sectionId] = play + 1
                LaneSection(
                    notation = notation,
                    startSeconds = leadSeconds + (onset + repeat * ticks) * secondsPerTick,
                    durationSeconds = ticks * secondsPerTick,
                    isRepeat = play > 0,
                )
            }.filterNotNull()
            onset += span.ticks
            laneSections
        }
        Lane(track.trackId, track.name, window.steps, leadSeconds, sections)
    }
}

/** The window of the lane of [track] in [song], with room for a line of lyrics when [isLyricsShown] and it has them. */
private fun laneWindowOf(track: Track, song: StoredSong, isLyricsShown: Boolean): StaffWindow {
    val lyricLines = if (isLyricsShown && song.tracks.lyricsOf(track) != null) 1 else 0
    return staffWindowOf(track, song.music.key, lyricLines = lyricLines)
}

private fun endSecondsOf(song: StoredSong, secondsPerTick: Double): Double {
    val ticks = song.tracks.sectionOrderOf(song.music).sumOf { song.tracks.lengthOf(it)?.ticks ?: 0 }
    return leadSecondsOf(song, secondsPerTick) + ticks * secondsPerTick
}

/** The length of the lead-in of [song]: its earliest pickup, and room before it for the head of the first note. */
private fun leadSecondsOf(song: StoredSong, secondsPerTick: Double): Double {
    val firstTick = songTonesOf(song.music, song.tracks, song.tracks).mapNotNull { it.firstTick }.minOrNull() ?: 0
    return -minOf(firstTick, 0) * secondsPerTick + LEAD_ROOM_SECONDS
}

/** This set with [value] added, or taken out when it holds it. */
private fun <T> Set<T>.toggled(value: T) = if (value in this) this - value else this + value

private const val DEFAULT_WINDOW_SECONDS = 12.0
private const val LEAD_ROOM_SECONDS = 0.5
