package streetlight.web.layout

import koala.component.heading2
import koala.layout.listItem
import koala.layout.olist
import koala.layout.section
import koala.layout.ulist
import kotlinx.html.FlowContent
import streetlight.model.data.LocationPost
import streetlight.web.shell.SectionHeadingMod

fun FlowContent.layoutLocationPosts(
    headingText: String?,
    posts: List<LocationPost>
) {
    section {
        headingText?.let {
            heading2(headingText, SectionHeadingMod)
        }
        ulist {
            posts.forEach {
                listItem {
                    largeLocationPostCard(it)
                }
            }
        }
    }
}