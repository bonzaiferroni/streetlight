package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class AreaTransit(
    val routes: List<TransitRoute>,
    val stops: List<TransitStop>
)