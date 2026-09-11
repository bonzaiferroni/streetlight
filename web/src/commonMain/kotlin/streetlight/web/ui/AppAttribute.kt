package streetlight.web.ui

import koala.html.stringAttributeOf
import koala.html.idAttributeOf
import streetlight.model.data.GalaxyId
import streetlight.model.data.MarkId
import streetlight.model.data.PostId

object AppAttribute {
    val GalaxyId = idAttributeOf("galaxy-id") { GalaxyId(it) }
    val PostId = idAttributeOf("post-id") { PostId(it) }
    val MarkId = idAttributeOf("mark-id") { MarkId(it) }

    val GalaxyName = stringAttributeOf("galaxy-name")
}