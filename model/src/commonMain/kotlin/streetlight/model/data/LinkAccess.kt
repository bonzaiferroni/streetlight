package streetlight.model.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The access a link's server granted, from its HTTP status and robots.txt. */
@Serializable
enum class LinkAccess {
    /** The page was served. */
    @SerialName("granted") Granted,
    /** robots.txt disallows the page's path. */
    @SerialName("robots-block") RobotsBlock,
    /** The server answered with a status other than 200 and below 500. */
    @SerialName("refused") Refused,
}
