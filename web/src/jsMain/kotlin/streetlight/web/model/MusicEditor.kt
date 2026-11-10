package streetlight.web.model

import kampfire.model.MutableTap
import kampfire.model.Tap
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import streetlight.model.music.Arrangement
import streetlight.model.music.Beats
import streetlight.model.music.LyricTrack
import streetlight.model.music.MelodyTrack
import streetlight.model.music.Music
import streetlight.model.music.MusicSection
import streetlight.model.music.Phrase
import streetlight.model.music.Section
import streetlight.model.music.SectionId
import streetlight.model.music.Track
import streetlight.model.music.TrackId
import streetlight.model.music.copyTrack
import streetlight.model.music.emptySectionOf
import streetlight.model.music.freeTrackIdOf
import streetlight.model.music.lengthOf
import streetlight.model.music.repeatsOf
import streetlight.model.music.sectionsOf
import streetlight.model.music.withSection
import streetlight.model.music.withoutSection
import streetlight.model.music.text.NotationStyle

/**
 * The state behind the editor of the music and tracks of [stored], written in the style at [notationStyleState], a
 * melody's notation showing its lyrics while [isLyricsShownState].
 */
class MusicEditor(
    private val scope: CoroutineScope,
    stored: StoredSong,
    val notationStyleState: Tap<NotationStyle>,
    val isLyricsShownState: MutableTap<Boolean>,
) {
    private val state = storeOf(MusicEditorState(stored.music, stored.tracks))
    val song = stored.song
    val musicState = state.mutableTapOf({ it.music }) { copy(music = it) }
    val tracksState = state.mutableTapOf({ it.tracks }) { copy(tracks = it) }

    /** The song as it is kept, following each change to its music and tracks. */
    val storedState = state.tapOf { StoredSong(song, it.music, it.tracks) }

    private val trackChangeFlow = MutableSharedFlow<TrackChange>(extraBufferCapacity = TRACK_CHANGE_BUFFER)

    /** Each track renamed or removed, as it happens. */
    val trackChanges: SharedFlow<TrackChange> = trackChangeFlow

    /** The warnings on the track [trackId]: each section it defines whose repeats fall short of another track's. */
    fun sectionWarningsState(trackId: TrackId) = state.tapOf { editor ->
        val tracks = editor.tracks
        val track = tracks.firstOrNull { it.trackId == trackId } ?: return@tapOf emptyList()
        val ids = track.sections.map { it.sectionId } + track.arrangements.map { it.sectionId }
        ids.mapNotNull { sectionId ->
            val length = track.lengthOf(sectionId) ?: return@mapNotNull null
            val longest = tracks.lengthOf(sectionId) ?: return@mapNotNull null
            val shortfall = longest.ticks - length.ticks * track.repeatsOf(sectionId, longest)
            if (shortfall <= 0) null
            else "${editor.music.nameOf(sectionId)} falls ${shortfall.toBeatsText()} beats short of another track after its repeats"
        }
    }

    fun readTrack(index: Int) = state.now.tracks.getOrNull(index)

    fun readTrack(trackId: TrackId) = state.now.tracks.firstOrNull { it.trackId == trackId }

    /** The track at [index]. */
    fun trackState(index: Int) = state.mutableTapOf({ it.tracks.getOrNull(index) }) { track ->
        if (track == null) this
        else copy(tracks = tracks.toMutableList().apply { set(index, track) })
    }

    /** The track [trackId]. */
    fun trackState(trackId: TrackId) = state.mutableTapOf({ editor -> editor.tracks.firstOrNull { it.trackId == trackId } }) { track ->
        if (track == null) this
        else copy(tracks = tracks.map { if (it.trackId == trackId) track else it })
    }

    /**
     * The track [trackId] as a [T], reading [empty] while there is no such track.
     *
     * A removal can reach the lens before the view reading it is rebuilt, which then reads [empty].
     */
    inline fun <reified T : Track> trackState(trackId: TrackId, empty: T): MutableTap<T> =
        trackState(trackId).mutableTapOf({ it as? T ?: empty }) { it }

    /** Whether the editor of the track [trackId] is open. */
    fun isTrackOpenState(trackId: TrackId) = state.tapOf { trackId in it.openTracks }

    /** A name for a new track that no track of the song holds: [base], or [base] with the first number that frees it. */
    fun newTrackId(base: String) = state.now.tracks.newTrackId(base)

    /** Adds [track] to the song and opens its editor. */
    fun addTrack(track: Track) = state.update { editor ->
        editor.copy(tracks = editor.tracks + track, openTracks = editor.openTracks + track.trackId)
    }

    /** Opens the editor of the track [trackId], or closes it when open. */
    fun toggleTrack(trackId: TrackId) = state.update { editor ->
        val openTracks = if (trackId in editor.openTracks) editor.openTracks - trackId else editor.openTracks + trackId
        editor.copy(openTracks = openTracks)
    }

    /**
     * Names the track [trackId] [name], numbered when another track holds it. A blank name leaves the track as it
     * is.
     */
    fun renameTrack(trackId: TrackId, name: String) {
        var change: TrackChange? = null
        state.update { editor ->
            val track = editor.tracks.firstOrNull { it.trackId == trackId } ?: return@update editor
            val base = name.trim().takeIf { it.isNotEmpty() && it != trackId.name } ?: return@update editor
            val renamedId = editor.tracks.filter { it.trackId != trackId }.newTrackId(base)
            val renamed = track.copyTrack(trackId = renamedId)
            editor.copy(
                tracks = editor.tracks.map { if (it.trackId == trackId) renamed else it },
                openTracks = if (trackId in editor.openTracks) editor.openTracks - trackId + renamedId else editor.openTracks,
            ).also { change = TrackChange.Renamed(trackId, renamedId) }
        }
        change?.let { trackChangeFlow.tryEmit(it) }
    }

    /** Removes the track [trackId]. */
    fun removeTrack(trackId: TrackId) {
        if (readTrack(trackId) == null) return
        state.update { editor ->
            editor.copy(
                tracks = editor.tracks.filter { it.trackId != trackId },
                openTracks = editor.openTracks - trackId,
            )
        }
        trackChangeFlow.tryEmit(TrackChange.Removed(trackId))
    }

    /** Sets the capo of the track [trackId], `null` for none. */
    fun setTrackCapo(trackId: TrackId, capo: Int?) = updateTrack(trackId) { it.copyTrack(capo = capo) }

    /** The sections the track [trackId] holds notation for, in its order, each with its name. */
    fun trackSectionsState(trackId: TrackId): Tap<List<MusicSection>> = state.tapOf { editor ->
        editor.tracks.firstOrNull { it.trackId == trackId }?.sections.orEmpty().map { editor.music.namedSectionOf(it.sectionId) }
    }

    /** The arrangements of the track [trackId], and the sections of the track they may play. */
    fun trackArrangementsState(trackId: TrackId): Tap<TrackArrangements> = state.tapOf { editor ->
        val track = editor.tracks.firstOrNull { it.trackId == trackId } ?: return@tapOf TrackArrangements()
        val music = editor.music
        TrackArrangements(
            arrangements = track.arrangements.map { arrangement ->
                NamedArrangement(music.namedSectionOf(arrangement.sectionId), arrangement.sections.map { music.namedSectionOf(it) })
            },
            choices = (track.sections.map { it.sectionId } + track.arrangements.map { it.sectionId }).map { music.namedSectionOf(it) },
        )
    }

    /** The arrangement of the music, each section with its name. */
    val arrangementState: Tap<List<MusicSection>> = state.tapOf { editor ->
        editor.music.arrangement.map { editor.music.namedSectionOf(it) }
    }

    /** The sections of the music. */
    val sectionsState: Tap<List<MusicSection>> = state.tapOf { it.music.sections }

    /** The stanzas and arrangements of stanzas a phrase may name as its lyrics: those of the first lyric track. */
    fun readLyricChoices(): List<MusicSection> {
        val lyrics = state.now.tracks.firstNotNullOfOrNull { it as? LyricTrack } ?: return emptyList()
        return (lyrics.sections.map { it.sectionId } + lyrics.arrangements.map { it.sectionId })
            .map { state.now.music.namedSectionOf(it) }
    }

    /**
     * Gives the track [trackId] an empty section for the section of the music named [name], which joins the music
     * when no section holds the name. Returns whether it was added: not for a blank name, or one the track holds.
     */
    fun addSection(trackId: TrackId, name: String) = addToTrack(trackId, name) { withSection(emptySectionOf(it)) }

    /** Gives the track [trackId] an empty arrangement for the section of the music named [name], as [addSection] does. */
    fun addArrangement(trackId: TrackId, name: String) =
        addToTrack(trackId, name) { copyTrack(arrangements = arrangements + Arrangement(it, emptyList())) }

    private fun addToTrack(trackId: TrackId, name: String, add: Track.(SectionId) -> Track): Boolean {
        val sectionName = name.trim().ifEmpty { return false }
        val track = readTrack(trackId) ?: return false
        val section = state.now.music.sectionOf(sectionName) ?: MusicSection(SectionId.random(), sectionName)
        if (track.sectionsOf(section.sectionId) != null || track.arrangements.any { it.sectionId == section.sectionId }) return false

        state.update { editor ->
            val sections = if (section in editor.music.sections) editor.music.sections else editor.music.sections + section
            editor.copy(
                music = editor.music.copy(sections = sections),
                tracks = editor.tracks.map { if (it.trackId == trackId) it.add(section.sectionId) else it },
            )
        }
        return true
    }

    /** Puts [section] in the track [trackId], in place of its section of the same id. */
    fun putSection(trackId: TrackId, section: Section) = updateTrack(trackId) { it.withSection(section) }

    /**
     * Removes the section or arrangement [sectionId] from the track [trackId], and from its arrangements. A section no
     * track holds any longer, and the music's arrangement does not play, leaves the music.
     */
    fun removeSection(trackId: TrackId, sectionId: SectionId) = state.update { editor ->
        val tracks = editor.tracks.map { track ->
            if (track.trackId != trackId) return@map track
            val removed = track.withoutSection(sectionId)
            removed.copyTrack(arrangements = removed.arrangements.map { it.copy(sections = it.sections - sectionId) })
        }
        val isHeld = sectionId in editor.music.arrangement ||
            tracks.any { track -> track.sections.any { it.sectionId == sectionId } || track.arrangements.any { it.sectionId == sectionId } }
        if (isHeld) return@update editor.copy(tracks = tracks)

        editor.copy(
            music = editor.music.copy(sections = editor.music.sections.filter { it.sectionId != sectionId }),
            tracks = tracks.map { track ->
                if (track !is MelodyTrack) track
                else track.copy(sections = track.sections.map { it.copy(lyrics = it.lyrics?.minus(sectionId)) })
            },
        )
    }

    /**
     * Names the section [sectionId] [name] in every track, numbered when another section holds the name. A blank name
     * leaves the section as it is.
     */
    fun renameSection(sectionId: SectionId, name: String) = state.update { editor ->
        val base = name.trim().takeIf { it.isNotEmpty() && it != editor.music.nameOf(sectionId) } ?: return@update editor
        val held = editor.music.sections.filter { it.sectionId != sectionId }.mapTo(mutableSetOf()) { it.name }
        val freeName = generateSequence(1) { it + 1 }.map { if (it == 1) base else "$base $it" }.first { it !in held }
        val sections = editor.music.sections.map { if (it.sectionId == sectionId) it.copy(name = freeName) else it }
        editor.copy(music = editor.music.copy(sections = sections))
    }

    /** Names the stanzas sung to the phrase [sectionId] of the track [trackId], `null` for any. */
    fun setSectionLyrics(trackId: TrackId, sectionId: SectionId, lyrics: List<SectionId>?) = updateTrack(trackId) { track ->
        val phrase = track.sections.firstOrNull { it.sectionId == sectionId } as? Phrase ?: return@updateTrack track
        track.withSection(phrase.copy(lyrics = lyrics))
    }

    /** Sets the sections the arrangement [sectionId] of the track [trackId] plays, in order. */
    fun setArrangement(trackId: TrackId, sectionId: SectionId, sections: List<SectionId>) = updateTrack(trackId) { track ->
        track.copyTrack(arrangements = track.arrangements.map {
            if (it.sectionId == sectionId) it.copy(sections = sections) else it
        })
    }

    /** Sets the sections every track follows, in order. */
    fun setMusicArrangement(sections: List<SectionId>) = state.update { it.copy(music = it.music.copy(arrangement = sections)) }

    private fun updateTrack(trackId: TrackId, transform: (Track) -> Track) = state.update { editor ->
        editor.copy(tracks = editor.tracks.map { if (it.trackId == trackId) transform(it) else it })
    }
}

/** The arrangement of [section] as it is shown: the [sections] it plays in order. */
data class NamedArrangement(
    val section: MusicSection,
    val sections: List<MusicSection>,
)

/** The [arrangements] of a track, and the [choices] of its sections an arrangement may play. */
data class TrackArrangements(
    val arrangements: List<NamedArrangement> = emptyList(),
    val choices: List<MusicSection> = emptyList(),
)

/** The section [sectionId] with the name this music knows it by. */
private fun Music.namedSectionOf(sectionId: SectionId) = MusicSection(sectionId, nameOf(sectionId))

data class MusicEditorState(
    val music: Music,
    val tracks: List<Track> = emptyList(),
    val openTracks: Set<TrackId> = emptySet(),
)

/** A track renamed or removed, which a reading of the tracks alone can't tell. */
sealed interface TrackChange {
    /** The track [from] now goes by [to]. */
    data class Renamed(val from: TrackId, val to: TrackId) : TrackChange

    /** The track [trackId] is gone. */
    data class Removed(val trackId: TrackId) : TrackChange
}

// the changes one edit can make, for a collector that falls behind
private const val TRACK_CHANGE_BUFFER = 64

private fun Int.toBeatsText() = (toDouble() / Beats.TICKS_PER_BEAT).let {
    if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
}

/** [base], or [base] with the first number from 2 that no track of these holds. */
private fun List<Track>.newTrackId(base: String) = freeTrackIdOf(base, mapTo(mutableSetOf()) { it.trackId })
