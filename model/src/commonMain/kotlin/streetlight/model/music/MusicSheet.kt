package streetlight.model.music

/** The [music] of a [song] with its [tracks], as it is read, written and exported whole. */
data class MusicSheet(
    val song: Song,
    val music: Music,
    val tracks: List<Track>,
)
