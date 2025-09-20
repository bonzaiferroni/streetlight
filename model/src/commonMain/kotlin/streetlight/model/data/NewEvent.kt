package streetlight.model.data

import kotlinx.datetime.Instant

data class NewEvent(
    val locationId: LocationId,
    val startsAt: Instant,
)