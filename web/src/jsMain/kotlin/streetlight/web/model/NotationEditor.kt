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
import streetlight.model.data.MusicParser
import streetlight.model.data.NotationError
import streetlight.model.data.NotationRead
import streetlight.model.data.NotationStyle

/**
 * The state behind the editor of the value at [valueState], read from the text the user writes.
 *
 * The text is read with [read] against [musicState] in [notationStyleState], and the value it produces replaces the
 * one at [valueState]. The text starts as [write] gives the value, and [write] gives the value back as text as it
 * changes.
 */
class NotationEditor<T>(
    scope: CoroutineScope,
    private val valueState: MutableTap<T>,
    private val musicState: Tap<Music?>,
    private val notationStyleState: Tap<NotationStyle>,
    private val read: (String, Music, NotationStyle) -> NotationRead<T>,
    private val write: (T, Music, NotationStyle) -> String,
) {
    private val state = storeOf(NotationEditorState(
        text = musicState.now?.let { write(valueState.now, it, notationStyleState.now) },
    ))
    val textState = state.mutableTapOf({ it.text.orEmpty() }) { copy(text = it) }
    val errorsState = state.tapOf { it.errors }

    /** The value written as the text it reads from, or `null` while there is no music. */
    val notationState = valueState
        .combine(musicState) { value, music -> music?.let { value to it } }
        .combine(notationStyleState) { pair, style -> pair?.let { (value, music) -> write(value, music, style) } }

    init {
        scope.launch {
            combine(
                state.flow.map { it.text }.filterNotNull().distinctUntilChanged(),
                musicState.flow.filterNotNull(),
                notationStyleState.flow,
            ) { text, music, style -> read(text, music, style) }
                .collect { read ->
                    valueState.set(read.value)
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
    valueState = model.trackState(index, ChordTrack(emptyList(), emptyList())),
    musicState = model.musicState,
    notationStyleState = model.notationStyleState,
    read = { text, music, style -> ChordParser(music.key, style).read(text, music.time) },
    write = { track, music, style -> ChordParser(music.key, style).write(track, music.time) },
)

/** A [NotationEditor] of the melody track at [index] of [model], written in jianpu. */
fun melodyEditorOf(scope: CoroutineScope, model: MusicEditor, index: Int) = NotationEditor(
    scope = scope,
    valueState = model.trackState(index, MelodyTrack(emptyList(), emptyList())),
    musicState = model.musicState,
    notationStyleState = model.notationStyleState,
    read = { text, music, _ -> MelodyParser(music.key).read(text, music.time) },
    write = { track, music, _ -> MelodyParser(music.key).write(track, music.time) },
)

/** A [NotationEditor] of the lyric track at [index] of [model]. */
fun lyricEditorOf(scope: CoroutineScope, model: MusicEditor, index: Int) = NotationEditor(
    scope = scope,
    valueState = model.trackState(index, LyricTrack(emptyList(), emptyList())),
    musicState = model.musicState,
    notationStyleState = model.notationStyleState,
    read = { text, music, _ -> LyricParser.read(text, music.time) },
    write = { track, music, _ -> LyricParser.write(track, music.time) },
)

/** A [NotationEditor] of the music of [model]'s loaded song. */
fun musicTextEditorOf(scope: CoroutineScope, model: MusicEditor) = NotationEditor(
    scope = scope,
    valueState = model.musicState,
    musicState = model.musicState,
    notationStyleState = model.notationStyleState,
    read = { text, music, _ -> MusicParser.read(text, music) },
    write = { _, music, _ -> MusicParser.write(music) },
)
