package streetlight.model.music

/** The [music] with its [tracks], in its track order, as it is edited, played and exported whole. */
data class MusicSheet(
    val music: Music,
    val tracks: List<Track>,
)
