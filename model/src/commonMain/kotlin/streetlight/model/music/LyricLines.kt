package streetlight.model.music

/** The lyric track sung to [track] in a song of these tracks: the one it names, or the first when it names none. */
fun List<Track>.lyricsOf(track: Track): LyricTrack? {
    if (track !is MelodyTrack) return null
    val lyricTracks = filterIsInstance<LyricTrack>()
    return if (track.lyricsId == null) lyricTracks.firstOrNull() else lyricTracks.firstOrNull { it.trackId == track.lyricsId }
}
