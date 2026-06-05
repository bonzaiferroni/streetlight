package streetlight.web.layouts

import koala.html.heading2
import koala.html.listItem
import koala.html.olist
import koala.html.section
import kotlinx.html.FlowContent
import streetlight.model.data.LocationPost
import streetlight.web.shells.SectionHeadingMod

fun FlowContent.layoutLocationPosts(
    headingText: String?,
    posts: List<LocationPost>
) {
    section {
        headingText?.let {
            heading2(headingText, SectionHeadingMod)
        }
        olist {
            posts.forEach {
                listItem {
                    largeLocationPostCard(it)
                }
            }
        }
    }
}