package kampfire.model

import kotlinx.serialization.Serializable

/** A two-dimensional vector. */
@Serializable
data class Vector2(
    val x: Double,
    val y: Double,
)