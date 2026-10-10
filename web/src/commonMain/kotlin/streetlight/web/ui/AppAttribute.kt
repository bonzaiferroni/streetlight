package streetlight.web.ui

import koala.modifier.stringAttributeOf
import koala.modifier.idAttributeOf
import koala.modifier.jsonAttributeOf
import streetlight.model.data.FeedSource
import streetlight.model.data.MarkId
import streetlight.model.data.PostId

/** The data attributes the app's scripts read from the page. */
object AppAttribute {
    val PostId = idAttributeOf("post-id") { PostId(it) }
    val MarkId = idAttributeOf("mark-id") { MarkId(it) }
    val FeedSource = jsonAttributeOf<FeedSource>("feed-source")

    val GalaxyName = stringAttributeOf("galaxy-name")
}