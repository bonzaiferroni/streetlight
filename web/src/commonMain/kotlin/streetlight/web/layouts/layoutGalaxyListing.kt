package streetlight.web.layouts

import koala.html.column
import kotlinx.html.FlowContent
import streetlight.model.data.GalaxyListing
import streetlight.model.data.PostType

fun FlowContent.layoutGalaxyListing(listing: GalaxyListing) {
    val types = listing.types
    when (types.size) {
        1 -> {
            val type = types.first()
            when (type) {
                PostType.Event -> layoutEventPosts(listing.events!!)
                PostType.Location -> TODO()
            }
        }
    }
}