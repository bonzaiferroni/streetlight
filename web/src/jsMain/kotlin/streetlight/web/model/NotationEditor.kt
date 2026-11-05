package streetlight.web.model

import kampfire.model.MutableTap
import kampfire.model.Tap
import kampfire.model.combine
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import streetlight.model.data.ChordParser
import streetlight.model.data.ChordTrack
import streetlight.model.data.LyricParser
import streetlight.model.data.LyricTrack
import streetlight.model.data.MelodyParser
import streetlight.model.data.MelodyTrack
import streetlight.model.data.Music
import streetlight.model.data.NotationError
import streetlight.model.data.NotationRead
import streetlight.model.data.NotationStyle
import streetlight.model.data.Track

/**
 * The state behind the editor of the track at [trackState], read from the text the user writes.
 *
 * The text is read with [read] against [musicState] in [notationStyleState], and the track it produces replaces the
 * one at [trackState]. The track is left alone until the text is first written. [write] gives the track back as text.
 */
class NotationEditor<T : Track>(
    scope: CoroutineScope,
    private val trackState: MutableTap<T>,
    private val musicState: Tap<Music?>,
    private val notationStyleState: Tap<NotationStyle>,
    private val read: (String, Music, NotationStyle) -> NotationRead<T>,
    private val write: (T, Music, NotationStyle) -> String,
) {
    private val state = storeOf(NotationEditorState())
    val textState = state.mutableTapOf({ it.text.orEmpty() }) { copy(text = it) }
    val errorsState = state.tapOf { it.errors }

    /** The track written as the text it reads from, or `null` while there is no music. */
    val notationState = trackState
        .combine(musicState) { track, music -> music?.let { track to it } }
        .combine(notationStyleState) { pair, style -> pair?.let { (track, music) -> write(track, music, style) } }

    init {
        scope.launch {
            combine(
                state.flow.map { it.text }.filterNotNull().distinctUntilChanged(),
                musicState.flow.filterNotNull(),
                notationStyleState.flow,
            ) { text, music, style -> read(text, music, style) }
                .collect { read ->
                    trackState.set(read.track)
                    state.update { it.copy(errors = read.errors) }
                }
        }
    }
}

data class NotationEditorState(
    val text: String? = null,
    val errors: List<NotationError> = emptyList(),
)

/** A [NotationEditor] of the chord track at [index] of [model], written in chord symbols. */
fun chordEditorOf(scope: CoroutineScope, model: MusicEditor, index: Int) = NotationEditor(
    scope = scope,
    trackState = model.trackState(index, ChordTrack(emptyList(), emptyList())),
    musicState = model.musicState,
    notationStyleState = model.notationStyleState,
    read = { text, music, style -> ChordParser(music.key, style).read(text, music.time) },
    write = { track, music, style -> ChordParser(music.key, style).write(track, music.time) },
)

/** A [NotationEditor] of the melody track at [index] of [model], written in jianpu. */
fun melodyEditorOf(scope: CoroutineScope, model: MusicEditor, index: Int) = NotationEditor(
    scope = scope,
    trackState = model.trackState(index, MelodyTrack(emptyList(), emptyList())),
    musicState = model.musicState,
    notationStyleState = model.notationStyleState,
    read = { text, music, _ -> MelodyParser(music.key).read(text, music.time) },
    write = { track, music, _ -> MelodyParser(music.key).write(track, music.time) },
)

/** A [NotationEditor] of the lyric track at [index] of [model]. */
fun lyricEditorOf(scope: CoroutineScope, model: MusicEditor, index: Int) = NotationEditor(
    scope = scope,
    trackState = model.trackState(index, LyricTrack(emptyList(), emptyList())),
    musicState = model.musicState,
    notationStyleState = model.notationStyleState,
    read = { text, music, _ -> LyricParser.read(text, music.time) },
    write = { track, music, _ -> LyricParser.write(track, music.time) },
)
