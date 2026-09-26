package streetlight.model.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The kind of content a link was found to hold. */
@Serializable
enum class LinkContent {
    /** Content a schema reads, of the link's schema type. */
    @SerialName("schema") Schema,
    /** Content on topic that no schema reads, such as a ticket page found where an event page was expected. */
    @SerialName("off-schema") OffSchema,
    /** Content off topic, such as an advertisement. */
    @SerialName("off-scope") OffScope,
    /** Content that could not be classified, such as a file that is not html. */
    @SerialName("unknown") Unknown,
    /** Content that needs scripting to appear. */
    @SerialName("unread") Unread,
}
