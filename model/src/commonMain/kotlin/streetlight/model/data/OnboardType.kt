package streetlight.model.data

import kampfire.model.Labeled
import kotlinx.serialization.Serializable

/** An audience the connect page pitches Streetlight to. */
@Serializable
enum class OnboardType(override val label: String): Labeled {
    BusinessOwner("Business Owner"),
    Performer("Performer"),
    Developer("Developer"),
    EventHost("Event Host"),
}
