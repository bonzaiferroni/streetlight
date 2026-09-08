package streetlight.web.ui

import koala.html.stringAttributeOf
import koala.html.uuidAttributeOf
import streetlight.model.data.GalaxyId
import streetlight.model.data.MarkId
import streetlight.model.data.PostId

object AppAttribute {
    val GalaxyId = uuidAttributeOf("galaxy-id") { GalaxyId(it) }
    val PostId = uuidAttributeOf("post-id") { PostId(it) }
    val MarkId = uuidAttributeOf("mark-id") { MarkId(it) }

    val GalaxyName = stringAttributeOf("galaxy-name")
}