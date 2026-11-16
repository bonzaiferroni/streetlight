package streetlight.model.music

/** A syllable or word of [text] that starts at [tick] of the notation it is sung to, or before it for a pickup. */
data class LineSyllable(
    val tick: Int,
    val text: String,
)

/** The lyric track sung to [track] in a song of these tracks: the one it names, or the first when it names none. */
fun List<Track>.lyricsOf(track: Track): LyricTrack? {
    if (track !is MelodyTrack) return null
    val lyricTracks = filterIsInstance<LyricTrack>()
    return if (track.lyrics == null) lyricTracks.firstOrNull() else lyricTracks.firstOrNull { it.trackId == track.lyrics }
}

/**
 * The lines of lyrics sung to [notationId] of [track] in a song of these tracks and [music]: one for each time the
 * track plays it, in order, holding the syllables of the same section that start during it. A stanza's pickup goes
 * with the play its stanza starts in. There are no lines when [lyricsOf] gives the track no lyrics.
 */
fun List<Track>.lyricLinesOf(track: Track, notationId: NotationId, music: Music): List<List<LineSyllable>> {
    val lyrics = lyricsOf(track) ?: return emptyList()
    val ticks = track.lengthOf(notationId)?.ticks ?: return emptyList()

    // place each syllable in the song, anchored to the tick and section that decide its line
    val anchored = mutableListOf<Pair<Play, LineSyllable>>()
    playsOf(lyrics, music).forEach { play ->
        val stanza = lyrics.notations.firstOrNull { it.notationId == play.notationId } as? Stanza ?: return@forEach
        var tick = play.tick - stanza.pickup.sumOf { it.beats.ticks }
        (stanza.pickup + stanza.notes).forEach { note ->
            note.text?.let { anchored.add(play.copy(tick = maxOf(tick, play.tick)) to LineSyllable(tick, it)) }
            tick += note.beats.ticks
        }
    }

    return playsOf(track, music).filter { it.notationId == notationId }.map { play ->
        anchored.filter { (anchor) -> anchor.slot == play.slot && anchor.tick in play.tick until play.tick + ticks }
            .map { (_, syllable) -> syllable.copy(tick = syllable.tick - play.tick) }
    }
}

/** A time a track starts [notationId], at [tick] of the song, within the [slot]th section of the arrangement. */
private data class Play(
    val notationId: NotationId,
    val tick: Int,
    val slot: Int,
)

/**
 * Each time [track] starts a notation or an arrangement in a song of [music], in order. An arrangement is followed by
 * the notations it holds.
 */
private fun playsOf(track: Track, music: Music): List<Play> {
    val plays = mutableListOf<Play>()
    // returns the length of what it placed
    fun place(notationId: NotationId, tick: Int, slot: Int): Int {
        plays.add(Play(notationId, tick, slot))
        track.notations.firstOrNull { it.notationId == notationId }?.let { return it.length.ticks }
        val arrangement = track.arrangements.firstOrNull { it.notationId == notationId } ?: return 0
        return arrangement.notations.fold(0) { length, heldId -> length + place(heldId, tick + length, slot) }
    }

    var onset = 0
    music.arrangement.forEachIndexed { slot, sectionId ->
        val span = music.lengthOf(sectionId) ?: Beats(0)
        val notationId = track.sections[sectionId]
        // a length means the notation resolves, so its arrangements hold no cycle
        notationId?.let { track.lengthOf(it) }?.let { length ->
            repeat(track.repeatsOf(notationId, span)) { place(notationId, onset + it * length.ticks, slot) }
        }
        onset += span.ticks
    }
    return plays
}
