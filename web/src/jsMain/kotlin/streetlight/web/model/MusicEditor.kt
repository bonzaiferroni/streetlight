package streetlight.web.model

import kampfire.model.reactIn
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import streetlight.model.music.LyricTrack
import streetlight.model.music.MelodyTrack
import streetlight.model.music.Music
import streetlight.model.music.Section
import streetlight.model.music.SectionId
import streetlight.model.music.Track
import streetlight.model.music.TrackId
import streetlight.model.music.TrackType
import streetlight.model.music.copyTrack
import streetlight.model.music.emptyTrackOf
import streetlight.model.music.freeTrackNameOf
import streetlight.model.music.ui.getMusicTable

class MusicEditor(
    private val scope: CoroutineScope,
    private val host: MusicHost,
) {
    private val state = storeOf(MusicEditorState())

    val focusState = state.tapOf { it.focus }

    val sheetState = host.sheetState
    val musicState = sheetState.tapOf { it.music }
    val tableState = sheetState.tapOf { it.getMusicTable() }
    val sectionsState = musicState.tapOf { it.sections }
    val arrangementState = musicState.tapOf { music ->
        music.arrangement.mapNotNull { sectionId -> music.sections.firstOrNull { it.sectionId == sectionId} }
    }
    val tracksState = sheetState.tapOf { it.tracks }
    val lyricTracksState = tracksState.tapOf { it.filterIsInstance<LyricTrack>() }

    fun readTrack(trackId: TrackId) = tracksState.now.firstOrNull { it.trackId == trackId }

    fun setFocus(focus: MusicEditorFocus?) = state.update { it.copy(focus = focus) }

    fun addTrack(trackType: TrackType) {
        val sheet = sheetState.now
        val track = emptyTrackOf(trackType, sheet.music.musicId, sheet.tracks.newTrackName(trackType.name))
        host.add(track)
        state.update { it.copy(focus = TrackFocus(track.trackId)) }
    }

    fun editTrack(trackId: TrackId, name: String, capo: Int?, lyrics: TrackId?) {
        val tracks = tracksState.now
        val trackNow = tracks.firstOrNull { it.trackId == trackId } ?: return
        val base = name.trim().takeIf { it.isNotEmpty() && it != trackNow.name }
        val freeName = base?.let { tracks.filter { it.trackId != trackId }.newTrackName(it) } ?: trackNow.name
        val track = trackNow.copyTrack(name = freeName, capo = capo).let { (it as? MelodyTrack)?.copy(lyricsId = lyrics) ?: it }
        host.update(track)
    }

    fun removeTrack(trackId: TrackId) {
        state.update { if (it.focus?.trackId == trackId) it.copy(focus = null) else it }
        host.remove(trackId)
    }

    fun setTrackOrder(tracks: List<Track>) {
        val trackOrder = tracks.map { it.trackId }
        val music = musicState.now.copy(trackOrder = trackOrder)
        host.update(music)
    }

    fun addSection(name: String, measures: Int): Boolean {
        val sectionName = name.trim().takeIf { it.isNotEmpty() && ',' !in it } ?: return false
        if (measures < 1) return false
        val musicNow = musicState.now
        val section = musicNow.sectionOf(sectionName) ?: Section(SectionId.random(), sectionName, measures)
        val sections = if (section in musicNow.sections) musicNow.sections else musicNow.sections + section
        val music = musicNow.copy(sections = sections, arrangement = musicNow.arrangement + section.sectionId)
        host.update(music)
        return true
    }

    fun editSection(sectionId: SectionId, name: String, measures: Int) {
        val musicNow = musicState.now
        val sectionsNow = musicNow.sections.filter { it.sectionId != sectionId }.mapTo(mutableSetOf()) { it.name }
        val base = name.trim().takeIf { it.isNotEmpty() && ',' !in it }
        val sections = musicNow.sections.map { section ->
            if (section.sectionId != sectionId) return@map section
            val freeName = base?.let { generateSequence(1) { it + 1 }.map { n -> if (n == 1) base else "$base $n" }.first { it !in sectionsNow } }
            section.copy(name = freeName ?: section.name, measures = measures.takeIf { it >= 1 } ?: section.measures)
        }
        host.update(musicNow.copy(sections = sections))
    }

    /** Sets the sections every track follows, in order. */
    fun setMusicArrangement(arrangement: List<SectionId>) {
        host.update(musicState.now.copy(arrangement = arrangement))
    }

    fun setMusic(music: Music) {
        host.update(music)
    }
}

data class MusicEditorState(
    val focus: MusicEditorFocus? = null,
)

/** [base], or [base] with the first number from 2 that no track of these holds. */
private fun List<Track>.newTrackName(base: String) = freeTrackNameOf(base, mapTo(mutableSetOf()) { it.name })
