package streetlight.model.music

import kotlinx.serialization.Serializable

@Serializable
data class LyricTrack(
    override val trackId: TrackId,
    override val musicId: MusicId,
    override val name: String,
    override val notations: List<Stanza>,
    override val arrangements: List<Arrangement>,
    override val sections: Map<SectionId, NotationId>,
    override val capo: Int? = null,
) : Track {
    override val trackType get() = TrackType.Lyrics
}

/** The [notes] of a lyric notation, led by a [pickup] that sounds at the end of the measure before it. */
@Serializable
data class Stanza(
    override val notationId: NotationId,
    override val name: String?,
    val notes: List<LyricNote>,
    val pickup: List<LyricNote> = emptyList(),
    override val measuresPerLine: Int = 2,
) : Notation {
    override val length get() = Beats(notes.sumOf { it.beats.ticks })
}

/** A syllable or word of [text] sung for [beats], or a rest when [text] is `null`. */
@Serializable
data class LyricNote(
    val text: String?,
    val beats: Beats,
)
