package streetlight.web.ui

import koala.html.stringAttributeOf
import koala.html.uuidAttributeOf
import streetlight.model.data.GalaxyId

object AppAttribute {
    val GalaxyId = uuidAttributeOf("galaxy-id") { GalaxyId(it) }
    val GalaxyName = stringAttributeOf("galaxy-name")
}