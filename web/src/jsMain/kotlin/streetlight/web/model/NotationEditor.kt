package streetlight.web.model

import kampfire.model.Tap
import kampfire.model.reactIn
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import streetlight.model.music.MelodyTrack
import streetlight.model.music.MusicSheet
import streetlight.model.music.NotationId
import streetlight.model.music.Phrase
import streetlight.model.music.lyricsOf
import streetlight.model.music.nameOf
import streetlight.model.music.notationOf
import streetlight.model.music.notationOrNullOf
import streetlight.model.music.sectionIdsOf
import streetlight.model.music.text.NotationError
import streetlight.model.music.text.NotationStyle
import streetlight.model.music.text.notationParseOf
import streetlight.model.music.trackOf
import streetlight.model.music.trackOrNullOf
import streetlight.model.music.ui.NotationLayout
import streetlight.model.music.ui.notationLayoutOf
import streetlight.model.music.withNotation

class NotationEditor(
    scope: CoroutineScope,
    notationStyleState: Tap<NotationStyle>,
    private val notationId: NotationId,
    private val host: MusicHost,
) {
    private val initialTrack = host.readTrack(notationId)
    private val trackId get() = initialTrack.trackId
    private val state = storeOf(NotationEditorState())
    val sheetState = host.sheetState
    val notationState = sheetState.tapOf { it.notationOf(notationId) }
    val notationLayoutState = sheetState.tapOf { it.notationLayoutOf(notationId) }
    val trackState = sheetState.tapOf { it.trackOf(notationId) }
    val nameState = trackState.tapOf { it.nameOf(notationId) }
    val errorsState = state.tapOf { it.errors }

    val notationTextEditor = NotationTextEditor(scope, notationId, host, notationStyleState)

    init {
        notationTextEditor.textState.reactIn(scope, 1) { text ->
            val notation = notationState.now
            val music = sheetState.now.music
            val parse = notationParseOf(
                notationId, initialTrack.trackType, notation.name, text, music.key, music.time, readPhraseNow()
            )

            state.set { copy(errors = parse.errors) }
            val track = host.readTrack(trackId).withNotation(parse.value)
            host.update(track)
        }
    }

    private fun readPhraseNow(): Phrase? {
        val tracks = sheetState.now.tracks
        val track = host.readTrack(trackId)
        val sectionId = track.sectionIdsOf(notationId).firstOrNull() ?: return null
        val melody = tracks.filterIsInstance<MelodyTrack>().firstOrNull { tracks.lyricsOf(it)?.trackId == track.trackId }
            ?: return null
        val phraseId = melody.sections[sectionId] ?: return null
        return melody.notations.firstOrNull { it.notationId == phraseId }
    }
}

data class NotationEditorState(
    val errors: List<NotationError> = emptyList()
)