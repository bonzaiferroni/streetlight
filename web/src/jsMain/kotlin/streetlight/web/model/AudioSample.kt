package streetlight.web.model

/**
 * A sample in the studio, named [name], whose PCM is kept apart under its [id]. It plays with its [filters] applied,
 * unless [isMuted].
 */
data class AudioSample(
    val id: Int,
    val name: String,
    val durationSeconds: Double,
    val isMuted: Boolean = false,
    val filters: Set<AudioFilter> = emptySet(),
)
