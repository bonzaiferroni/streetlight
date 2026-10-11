package streetlight.web.page

import koala.modifier.enumAttributeOf
import koala.modifier.stringAttributeOf
import koala.modifier.idAttributeOf
import koala.modifier.jsonAttributeOf
import streetlight.model.data.ConnectType
import streetlight.model.data.FeedSource
import streetlight.model.data.MarkId
import streetlight.model.data.PostId

/** The data attributes the app's scripts read from the page. */
object AppAttribute {
    val PostId = idAttributeOf("post-id") { PostId(it) }
    val MarkId = idAttributeOf("mark-id") { MarkId(it) }
    val FeedSource = jsonAttributeOf<FeedSource>("feed-source")

    val GalaxyName = stringAttributeOf("galaxy-name")

    /** The connect type chosen on the page, set on an ancestor of the connect picker and panels. */
    val ConnectType = enumAttributeOf<ConnectType>("connect-type")
    /** The connect type a picker option or panel stands for. */
    val ConnectOption = enumAttributeOf<ConnectType>("connect-option")
}