package streetlight.web.model

import kampfire.model.MutableTap
import kampfire.model.Tap
import kampfire.model.reactIn
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import streetlight.model.music.MelodyTrack
import streetlight.model.music.MusicSheet
import streetlight.model.music.Notation
import streetlight.model.music.NotationId
import streetlight.model.music.Phrase
import streetlight.model.music.TrackId
import streetlight.model.music.lyricsOf
import streetlight.model.music.sectionIdsOf
import streetlight.model.music.text.NotationStyle
import streetlight.model.music.text.readNotation
import streetlight.model.music.text.writeNotation
import streetlight.model.music.ui.SectionNotation
import streetlight.model.music.ui.sectionNotationsOf

/**
 * The state behind the editor of [notationState], the notation [notationId] of the track [trackId] in [sheetState],
 * read from the text of its track type in the style of [notationStyleState]. A read replaces the notation's notes and
 * leaves the rest of it alone. A change of key, time or style, or an edit to its notes from elsewhere, rewrites the
 * text from the notation.
 */
class NotationEditor(
    scope: CoroutineScope,
    notationStyleState: Tap<NotationStyle>,
    private val sheetState: Tap<MusicSheet>,
    private val notationState: MutableTap<Notation?>,
    private val trackId: TrackId,
    private val notationId: NotationId,
) {
    private val state = storeOf(NotationEditorState())
    private val field = NotationField(writeNow())
    private var lastRead: Notation? = null

    val textState = field.textState
    val errorsState = field.errorsState
    val sectionNotationState = state.tapOf { it.sectionNotation }

    init {
        sheetState.reactIn(scope) { sheet -> state.update { it.copy(sectionNotation = sectionNotationOf(sheet)) } }
        // a change of key transposes the notation, and a change of time or style respells it, so the text follows it
        val changes = combine(sheetState.flow, notationStyleState.flow) { sheet, style ->
            Triple(sheet.music.key, sheet.music.time, style)
        }
        field.launchRewrites(scope, changes.distinctUntilChanged()) { writeNow() }
        // an edit from elsewhere replaces the notes under the text, which a later read would otherwise undo
        scope.launch {
            notationState.flow.filter { isChangedElsewhere() }.collect { field.setText(writeNow()) }
        }
        field.launchReads(scope) { text ->
            val notation = notationState.now ?: return@launchReads emptyList()
            val read = readNotation(notation, text, sheetState.now.music, readPhraseNow())
            lastRead = read.value
            notationState.set(read.value)
            read.errors
        }
    }

    // the notation drawn with the lyrics sung to it, or `null` once it is gone
    private fun sectionNotationOf(sheet: MusicSheet): SectionNotation? {
        val track = sheet.tracks.firstOrNull { it.trackId == trackId } ?: return null
        return sectionNotationsOf(track, sheet.music, sheet.tracks).firstOrNull { it.notationId == notationId }
    }

    // the phrase a stanza is sung with: the one a melody singing this track plays for the stanza's first section
    private fun readPhraseNow(): Phrase? {
        val tracks = sheetState.now.tracks
        val track = tracks.firstOrNull { it.trackId == trackId } ?: return null
        val sectionId = track.sectionIdsOf(notationId).firstOrNull() ?: return null
        val melody = tracks.filterIsInstance<MelodyTrack>().firstOrNull { tracks.lyricsOf(it)?.trackId == trackId }
            ?: return null
        val phraseId = melody.sections[sectionId] ?: return null
        return melody.notations.firstOrNull { it.notationId == phraseId }
    }

    private fun write(notation: Notation) = writeNotation(notation, sheetState.now.music)

    private fun writeNow() = notationState.now?.let { write(it) }

    // only the notes count, so a change to what the text does not state leaves it alone
    private fun isChangedElsewhere(): Boolean {
        val read = lastRead ?: return false
        val notation = notationState.now ?: return false
        return write(notation) != write(read)
    }
}

data class NotationEditorState(
    val sectionNotation: SectionNotation? = null,
)
