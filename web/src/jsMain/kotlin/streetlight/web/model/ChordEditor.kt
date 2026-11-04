package streetlight.web.model

import kampfire.model.MutableTap
import kampfire.model.combine
import kampfire.model.Tap
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import streetlight.model.data.ChordTrack
import streetlight.model.data.Music
import streetlight.model.data.NotationError
import streetlight.model.data.NotationStyle
import streetlight.model.data.readChordTrack
import streetlight.model.data.writeChordTrack

/**
 * The state behind the editor of the chord track at [trackState], read from the text the user writes.
 *
 * The text is read against [musicState] in [notationStyleState], and the track it produces replaces the one at
 * [trackState]. The track is left alone until the text is first written.
 */
class ChordEditor(
    scope: CoroutineScope,
    private val trackState: MutableTap<ChordTrack>,
    private val musicState: Tap<Music?>,
    private val notationStyleState: Tap<NotationStyle>,
) {
    private val state = storeOf(ChordEditorState())
    val textState = state.mutableTapOf({ it.text.orEmpty() }) { copy(text = it) }
    val errorsState = state.tapOf { it.errors }

    /** The track written as the text it reads from, or `null` while there is no music. */
    val notationState = trackState
        .combine(musicState) { track, music -> music?.let { track to it } }
        .combine(notationStyleState) { pair, style ->
            pair?.let { (track, music) -> writeChordTrack(track, music.time, music.key, style) }
        }

    init {
        scope.launch {
            combine(
                state.flow.map { it.text }.filterNotNull().distinctUntilChanged(),
                musicState.flow.filterNotNull(),
                notationStyleState.flow,
            ) { text, music, style -> readChordTrack(text, music.time, music.key, style) }
                .collect { read ->
                    trackState.set(read.track)
                    state.update { it.copy(errors = read.errors) }
                }
        }
    }
}

data class ChordEditorState(
    val text: String? = null,
    val errors: List<NotationError> = emptyList(),
)
