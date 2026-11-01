package streetlight.model.data

import kotlinx.serialization.Serializable

/** The details of a [Media] particular to its [MediaType], as [Audio] for an audio post. */
@Serializable
sealed interface MediaMeta
