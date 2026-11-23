package streetlight.model.music

/** A syllable or word of [text] that starts at [tick] of the notation it is sung to, or before it for a pickup. */
data class LineVocal(
    val tick: Int,
    val text: String,
)

/** The lyric track sung to [track] in a song of these tracks: the one it names, or the first when it names none. */
fun List<Track>.lyricsOf(track: Track): LyricTrack? {
    if (track !is MelodyTrack) return null
    val lyricTracks = filterIsInstance<LyricTrack>()
    return if (track.lyricsId == null) lyricTracks.firstOrNull() else lyricTracks.firstOrNull { it.trackId == track.lyricsId }
}

/**
 * The lines of lyrics sung to [notationId] in [sheet]: one for each time its track plays it, in order, holding the
 * syllables of the same section that start during it. A stanza's pickup goes with the pass its stanza starts in.
 * There are no lines when [lyricsOf] gives the track no lyrics.
 */
fun lyricLinesOf(notationId: NotationId, sheet: MusicSheet): List<List<LineVocal>> {
    val track = sheet.trackOf(notationId)
    val lyrics = sheet.tracks.lyricsOf(track) ?: return emptyList()
    val ticks = track.lengthOf(notationId)?.ticks ?: return emptyList()

    // place each syllable in the song, anchored to the tick and section that decide its line
    val anchored = mutableListOf<Pair<NotationPass, LineVocal>>()
    passesOf(lyrics, sheet.music).forEach { pass ->
        val stanza = lyrics.notations.firstOrNull { it.notationId == pass.notationId } ?: return@forEach
        var tick = pass.tick - stanza.pickup.sumOf { it.beats.ticks }
        (stanza.pickup + stanza.notes).forEach { note ->
            note.text?.let { anchored.add(pass.copy(tick = maxOf(tick, pass.tick)) to LineVocal(tick, it)) }
            tick += note.beats.ticks
        }
    }

    return passesOf(track, sheet.music).filter { it.notationId == notationId }.map { pass ->
        anchored.filter { (anchor) -> anchor.slot == pass.slot && anchor.tick in pass.tick until pass.tick + ticks }
            .map { (_, syllable) -> syllable.copy(tick = syllable.tick - pass.tick) }
    }
}

/** A time a track starts [notationId], at [tick] of the song, within the [slot]th section of the arrangement. */
private data class NotationPass(
    val notationId: NotationId,
    val tick: Int,
    val slot: Int,
)

/**
 * Each time [track] starts a notation or an arrangement in a song of [music], in order. An arrangement is followed by
 * the notations it holds.
 */
private fun passesOf(track: Track, music: Music): List<NotationPass> {
    val passes = mutableListOf<NotationPass>()
    // returns the length of what it placed
    fun place(notationId: NotationId, tick: Int, slot: Int): Int {
        passes.add(NotationPass(notationId, tick, slot))
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
    return passes
}
