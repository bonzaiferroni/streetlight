package streetlight.model.music

/** The [music] with its [tracks], in its track order, as it is edited, played and exported whole. */
data class MusicSheet(
    val music: Music,
    val tracks: List<Track>,
)

fun MusicSheet.trackOrNullOf(notationId: NotationId) = tracks.firstOrNull { track ->
    track.notations.any { it.notationId == notationId } || track.arrangements.any { it.notationId == notationId }
}

fun MusicSheet.trackOrNullOf(trackId: TrackId) = tracks.firstOrNull { it.trackId == trackId }

fun MusicSheet.trackOf(trackId: TrackId) = trackOrNullOf(trackId) ?: error("track not found")

fun MusicSheet.trackOf(notationId: NotationId) = trackOrNullOf(notationId) ?: error("track not found")

fun MusicSheet.notationOrNullOf(notationId: NotationId) =
    tracks.firstNotNullOfOrNull { tracks -> tracks.notations.firstOrNull { it.notationId == notationId } }

fun MusicSheet.notationOf(notationId: NotationId) = notationOrNullOf(notationId) ?: error("notation not found")