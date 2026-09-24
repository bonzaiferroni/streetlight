package streetlight.web.layouts

import koala.modifier.*
import koala.html.Id
import koala.html.column
import koala.html.heading3
import koala.html.tabs
import kotlinx.html.FlowContent
import streetlight.model.data.PostListing
import streetlight.model.data.PostType

/** The posts of a galaxy, as one list for one post type or a tab for each. */
fun FlowContent.layoutPostListing(types: Set<PostType>, listing: PostListing) {
    when (types.size) {
        0 -> {
            column {
                heading3("This galaxy quietly waits for posts", modify(TextAlignCenter, OpacityHalf))
            }
        }
        1 -> {
            val type = types.first()
            when (type) {
                PostType.Event -> layoutEventPosts("Upcoming Events", listing.events)
                PostType.Location -> layoutLocationPosts("Locations", listing.locations)
                else -> { }
            }
        }
        else -> {
            tabs(GalaxyPostListing.Id) {
                types.forEach { type ->
                    when (type) {
                        PostType.Event -> {
                            tab("Events") {
                                layoutEventPosts(null, listing.events)
                            }
                        }
                        PostType.Location -> {
                            tab("Locations") {
                                layoutLocationPosts(null, listing.locations)
                            }
                        }
                        else -> { }
                    }
                }
            }
        }
    }
}

object GalaxyPostListing {
    val Id = Id("galaxy-listing")
}