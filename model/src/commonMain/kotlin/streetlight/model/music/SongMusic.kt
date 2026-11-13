package streetlight.model.music

/** The [music] with the [song] it is of, as a list of musics shows it. */
data class SongMusic(
    val song: Song,
    val music: Music,
)
