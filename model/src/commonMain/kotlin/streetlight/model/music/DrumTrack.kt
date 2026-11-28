package streetlight.model.music

import kotlinx.serialization.Serializable

@Serializable
data class DrumTrack(
    override val trackId: TrackId,
    override val musicId: MusicId,
    override val name: String,
    override val notations: List<Groove>,
    override val arrangements: List<Arrangement>,
    override val sections: Map<SectionId, NotationId>,
    override val capo: Int? = null,
) : Track {
    override val trackType get() = TrackType.Drums
}

/**
 * The [lanes] of a drum notation, one per drum, sounding together. [slotsPerMeasure] and [measuresPerLine] are the
 * layout of its tab.
 */
@Serializable
data class Groove(
    override val notationId: NotationId,
    override val name: String?,
    val lanes: List<DrumLane>,
    val slotsPerMeasure: Int = 16,
    override val measuresPerLine: Int = 2,
) : Notation {
    override val downbeat get() = Beats(0)
    override val length get() = Beats(lanes.maxOfOrNull { lane -> lane.beats.sumOf { it.beats.ticks } } ?: 0)
    override val trackType get() = TrackType.Drums
}

/** The [beats] one [drum] plays in a groove. */
@Serializable
data class DrumLane(
    val drum: Drum,
    val beats: List<DrumBeat>,
)

/** A [hit] that lasts for [beats] until the next, or a rest when [hit] is `null`. */
@Serializable
data class DrumBeat(
    val hit: DrumHit?,
    val beats: Beats,
)

/** How a drum is struck. */
enum class DrumHit {
    Normal,
    Accent,
    Ghost,
}

enum class Drum {
    Kick,
    Snare,
    HiHat,
    Crash,
    Ride,
    Tom,
    Sticks,
}
