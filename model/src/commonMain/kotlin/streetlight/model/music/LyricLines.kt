package streetlight.model.music

/** A syllable or word of [text] that starts at [tick] of the section it is sung to, or before the section for a pickup. */
data class LineSyllable(
    val tick: Int,
    val text: String,
)

/** The lyric track sung to [track] in a song of these tracks: the first, when [track] is a melody track. */
fun List<Track>.lyricsOf(track: Track): LyricTrack? {
    if (track !is MelodyTrack) return null
    return firstNotNullOfOrNull { it as? LyricTrack }
}

/**
 * The lines of lyrics sung to [sectionId] of [track] in a song of these tracks and [music]: one for each time the track
 * plays the section, in order, holding the syllables that start during it. A stanza's pickup goes with the play its
 * stanza starts in, and a phrase that names its [Phrase.lyrics] leaves out the syllables of any other stanza. There are
 * no lines when [lyricsOf] gives the track no lyrics.
 */
fun List<Track>.lyricLinesOf(track: Track, sectionId: SectionId, music: Music): List<List<LineSyllable>> {
    val lyrics = lyricsOf(track) ?: return emptyList()
    val ticks = track.lengthOf(sectionId)?.ticks ?: return emptyList()
    val plays = playsOf(track, music)

    // find the stanzas each play of a phrase admits, `null` for any
    val admissions = plays.mapNotNull { (playedId, start) ->
        val phrase = track.sections.firstOrNull { it.sectionId == playedId } as? Phrase ?: return@mapNotNull null
        val stanzaIds = phrase.lyrics?.flatMap { lyrics.sectionsOf(it).orEmpty() }?.map { it.sectionId }?.toSet()
        Admission(start until start + phrase.length.ticks, stanzaIds)
    }

    // place each syllable in the song, anchored to the tick that decides its line
    val anchored = mutableListOf<Pair<Int, LineSyllable>>()
    playsOf(lyrics, music).forEach { (stanzaId, start) ->
        val stanza = lyrics.sections.firstOrNull { it.sectionId == stanzaId } ?: return@forEach
        var tick = start - stanza.pickup.sumOf { it.beats.ticks }
        (stanza.pickup + stanza.notes).forEach { note ->
            val anchor = maxOf(tick, start)
            val isAdmitted = admissions.firstOrNull { anchor in it.ticks }?.stanzaIds?.let { stanzaId in it } ?: true
            if (isAdmitted) note.text?.let { anchored.add(anchor to LineSyllable(tick, it)) }
            tick += note.beats.ticks
        }
    }

    return plays.filter { (playedId) -> playedId == sectionId }.map { (_, start) ->
        anchored.filter { (anchor) -> anchor in start until start + ticks }
            .map { (_, syllable) -> syllable.copy(tick = syllable.tick - start) }
    }
}

/** The [stanzaIds] a play of a phrase lets be sung through its [ticks] of the song, `null` for any. */
private class Admission(
    val ticks: IntRange,
    val stanzaIds: Set<SectionId>?,
)

/**
 * Each time [track] starts a section or an arrangement in a song of these tracks and [music], in order, paired with the
 * tick of the song it starts at. An arrangement is followed by the sections it holds.
 */
private fun List<Track>.playsOf(track: Track, music: Music): List<Pair<SectionId, Int>> {
    val plays = mutableListOf<Pair<SectionId, Int>>()
    // returns the length of what it placed
    fun place(sectionId: SectionId, tick: Int): Int {
        plays.add(sectionId to tick)
        track.sections.firstOrNull { it.sectionId == sectionId }?.let { return it.length.ticks }
        val arrangement = track.arrangements.firstOrNull { it.sectionId == sectionId } ?: return 0
        return arrangement.sections.fold(0) { length, heldId -> length + place(heldId, tick + length) }
    }

    var onset = 0
    sectionOrderOf(music).forEach { sectionId ->
        val span = lengthOf(sectionId) ?: Beats(0)
        // a length means the section resolves, so its arrangements hold no cycle
        track.lengthOf(sectionId)?.let { length ->
            repeat(track.repeatsOf(sectionId, span)) { place(sectionId, onset + it * length.ticks) }
        }
        onset += span.ticks
    }
    return plays
}
