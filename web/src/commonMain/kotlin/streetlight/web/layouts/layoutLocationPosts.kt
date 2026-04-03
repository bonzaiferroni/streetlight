package streetlight.web.layouts

import kabinet.utils.toRelativeDayFormat
import koala.css.MarginTop2
import koala.css.OpacityMost
import koala.css.modify
import koala.html.column
import koala.html.filigree
import koala.html.heading2
import koala.html.heading4
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