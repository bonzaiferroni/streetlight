package streetlight.web.model

import kampfire.model.Tap
import kampfire.model.reactIn
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import streetlight.model.music.MelodyTrack
import streetlight.model.music.MusicSheet
import streetlight.model.music.Notation
import streetlight.model.music.NotationId
import streetlight.model.music.Phrase
import streetlight.model.music.Track
import streetlight.model.music.lyricsOf
import streetlight.model.music.nameOf
import streetlight.model.music.sectionIdsOf
import streetlight.model.music.text.NotationStyle
import streetlight.model.music.text.readNotation
import streetlight.model.music.text.writeNotation
import streetlight.model.music.ui.NotationLayout
import streetlight.model.music.ui.notationLayoutsOf
import streetlight.model.music.withNotation
import streetlight.model.studio.StudioDb

/**
 * The state behind the editor of the notation [notationId] of [initialTrack], read from the text of its track type in
 * the style of [notationStyleState] and drawn against the song of [sheetState]. A read replaces the notation's notes
 * and leaves the rest of it alone, and a change of key, time or style rewrites the text. The track is saved to [db]
 * once edits pause and once more as its scope ends.
 */
class NotationEditor(
    scope: CoroutineScope,
    notationStyleState: Tap<NotationStyle>,
    initialTrack: Track,
    private val notationId: NotationId,
    private val sheetState: Tap<MusicSheet>,
    db: StudioDb,
) {
    // drawn from the start, so the view transition that opens the editor captures its notes
    private val state = storeOf(NotationEditorState(initialTrack, notationLayoutOf(initialTrack, sheetState.now)))
    private val field = NotationField(writeNow())

    val trackState = state.tapOf { it.track }
    val nameState = state.tapOf { it.track.nameOf(notationId, sheetState.now.music) }
    val textState = field.textState
    val errorsState = field.errorsState
    val notationLayoutState = state.tapOf { it.notationLayout }

    /** The track as it stands, with every edit made so far. */
    val trackNow get() = state.now.track

    init {
        scope.launch { trackState.saveChanges { db.track.saveTrack(it) } }
        sheetState.reactIn(scope) { sheet -> state.update { it.copy(notationLayout = notationLayoutOf(it.track, sheet)) } }
        // a change of key transposes the notation, and a change of time or style respells it, so the text follows it
        val changes = combine(sheetState.flow, notationStyleState.flow) { sheet, style ->
            Triple(sheet.music.key, sheet.music.time, style)
        }
        field.launchRewrites(scope, changes.distinctUntilChanged()) { writeNow() }
        field.launchReads(scope) { text ->
            val notation = readNow() ?: return@launchReads emptyList()
            val read = readNotation(notation, text, sheetState.now.music, readPhraseNow())
            state.update { editor ->
                val track = editor.track.withNotation(read.value)
                editor.copy(track = track, notationLayout = notationLayoutOf(track, sheetState.now))
            }
            read.errors
        }
    }

    private fun readNow(): Notation? = state.now.track.notations.firstOrNull { it.notationId == notationId }

    // drawn against the song for the lyrics sung to it, with this editor's own track
    private fun notationLayoutOf(track: Track, sheet: MusicSheet): NotationLayout? =
        notationLayoutsOf(track, sheet.music, sheet.tracks).firstOrNull { it.notationId == notationId }

    // the phrase a stanza is sung with: the one a melody singing this track plays for the stanza's first section
    private fun readPhraseNow(): Phrase? {
        val tracks = sheetState.now.tracks
        val track = state.now.track
        val sectionId = track.sectionIdsOf(notationId).firstOrNull() ?: return null
        val melody = tracks.filterIsInstance<MelodyTrack>().firstOrNull { tracks.lyricsOf(it)?.trackId == track.trackId }
            ?: return null
        val phraseId = melody.sections[sectionId] ?: return null
        return melody.notations.firstOrNull { it.notationId == phraseId }
    }

    private fun writeNow() = readNow()?.let { writeNotation(it, sheetState.now.music) }
}

data class NotationEditorState(
    val track: Track,
    val notationLayout: NotationLayout? = null,
)
