package streetlight.model.data

import kotlinx.serialization.Serializable

/** The transit routes and stops of an area. */
@Serializable
data class AreaTransit(
    val routes: List<TransitRoute>,
    val stops: List<TransitStop>
)