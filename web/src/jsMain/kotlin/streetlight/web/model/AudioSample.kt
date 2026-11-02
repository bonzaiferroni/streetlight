package streetlight.web.model

/**
 * A sample in the studio, named [name], whose PCM is kept apart under its [id]. It begins at [startSeconds] on the
 * timeline and plays with its [filters] applied, unless [isMuted].
 */
data class AudioSample(
    val id: Int,
    val name: String,
    val durationSeconds: Double,
    val startSeconds: Double = 0.0,
    val isMuted: Boolean = false,
    val filters: Set<AudioFilter> = emptySet(),
) {
    val endSeconds get() = startSeconds + durationSeconds
}
