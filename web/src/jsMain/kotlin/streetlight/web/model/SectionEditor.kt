package streetlight.web.model

import kampfire.model.Tap
import kampfire.model.combine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import streetlight.model.music.MelodyTrack
import streetlight.model.music.MusicSheet
import streetlight.model.music.Section
import streetlight.model.music.SectionId
import streetlight.model.music.TrackId
import streetlight.model.music.text.readSection
import streetlight.model.music.text.writeSection
import streetlight.model.music.ui.SectionNotation
import streetlight.model.music.ui.sectionNotationsOf

/**
 * The state behind the editor of the section [sectionId] of the track [trackId] of [model], read from the notation of
 * its track type. A read replaces the section's notes and leaves the rest of it alone. A change of key, time or style,
 * or an edit to its notes from elsewhere, rewrites the text from the section.
 */
class SectionEditor(
    scope: CoroutineScope,
    private val model: MusicEditor,
    private val trackId: TrackId,
    private val sectionId: SectionId,
) {
    private val field = NotationField(writeNow())
    private var lastRead: Section? = null
    val textState = field.textState
    val errorsState = field.errorsState

    /** The notation of the section, with the lyrics sung to it while they are shown, or `null` once it is gone. */
    val notationState: Tap<SectionNotation?> = model.sheetState.combine(model.isLyricsShownState) { sheet, isLyricsShown ->
        val track = sheet.tracks.firstOrNull { it.trackId == trackId } ?: return@combine null
        sectionNotationsOf(track, sheet.music, sheet.tracks, isLyricsShown).firstOrNull { it.sectionId == sectionId }
    }

    init {
        // a change of key transposes the section, and a change of time or style respells it, so the text follows it
        val changes = combine(model.musicState.flow, model.notationStyleState.flow) { music, style ->
            Triple(music.key, music.time, style)
        }
        field.launchRewrites(scope, changes.distinctUntilChanged()) { writeNow() }
        // an edit from elsewhere replaces the notes under the text, which a later read would otherwise undo
        scope.launch {
            model.sheetState.flow.filter { isChangedElsewhere() }.collect { field.setText(writeNow()) }
        }
        field.launchReads(scope) { text ->
            val section = readNow() ?: return@launchReads emptyList()
            val melody = model.tracksState.now.firstNotNullOfOrNull { it as? MelodyTrack }
            val read = readSection(section, text, model.musicState.now, model.notationStyleState.now, melody)
            lastRead = read.value
            model.putSection(trackId, read.value)
            read.errors
        }
    }

    private fun readNow() = model.readTrack(trackId)?.sections?.firstOrNull { it.sectionId == sectionId }

    private fun write(section: Section) = writeSection(section, model.musicState.now, model.notationStyleState.now)

    private fun writeNow() = readNow()?.let { write(it) }

    // only the notes count, so a change to what the text does not state leaves it alone
    private fun isChangedElsewhere(): Boolean {
        val read = lastRead ?: return false
        val section = readNow() ?: return false
        return write(section) != write(read)
    }
}
