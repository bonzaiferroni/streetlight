package streetlight.web.layouts

import kabinet.utils.toRelativeDayFormat
import koala.html.listItem
import koala.html.olist
import koala.html.section
import koala.html.sectionHeading
import kotlinx.html.FlowContent
import streetlight.model.data.GalaxyPost

fun FlowContent.layoutGalaxyPosts(posts: List<GalaxyPost>) {
    val groupings = posts.groupBy { it.event?.startsAt }
    groupings.forEach { grouping ->
        val startsAt = grouping.key ?: return@forEach
        val posts = grouping.value
        section {
            sectionHeading(startsAt.toRelativeDayFormat())
            olist {
                posts.forEach {
                    listItem {
                        layoutLargeGalaxyPost(it)
                    }
                }
            }
        }
    }
}