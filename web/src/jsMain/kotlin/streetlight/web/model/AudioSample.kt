package streetlight.web.model

/** A sample in the studio, named [name], whose PCM is kept apart under its [id]. */
data class AudioSample(
    val id: Int,
    val name: String,
    val durationSeconds: Double,
)
