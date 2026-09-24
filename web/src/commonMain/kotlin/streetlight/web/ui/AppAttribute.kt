package streetlight.web.ui

import koala.modifier.stringAttributeOf
import koala.modifier.idAttributeOf
import streetlight.model.data.CityId
import streetlight.model.data.GalaxyId
import streetlight.model.data.MarkId
import streetlight.model.data.PostId

/** The data attributes the app's scripts read from the page. */
object AppAttribute {
    val GalaxyId = idAttributeOf("galaxy-id") { GalaxyId(it) }
    val CityId = idAttributeOf("city-id") { CityId(it) }
    val PostId = idAttributeOf("post-id") { PostId(it) }
    val MarkId = idAttributeOf("mark-id") { MarkId(it) }

    val GalaxyName = stringAttributeOf("galaxy-name")
}