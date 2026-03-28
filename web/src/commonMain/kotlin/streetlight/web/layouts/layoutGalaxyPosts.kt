package streetlight.web.layouts

import kabinet.utils.toRelativeDayFormat
import koala.css.OpacityHalf
import koala.css.OpacityMost
import koala.html.column
import koala.html.filigree
import koala.html.heading2
import koala.html.heading3
import koala.html.heading4
import koala.html.listItem
import koala.html.olist
import koala.html.section
import koala.html.sectionHeading
import kotlinx.html.FlowContent
import streetlight.model.data.GalaxyPost
import streetlight.web.shells.SectionHeadingMods

fun FlowContent.layoutGalaxyPosts(posts: List<GalaxyPost>) {
    column {
        heading2("Upcoming Events", SectionHeadingMods)
        val groupings = posts.groupBy { it.event?.startsAt }
        groupings.forEach { grouping ->
            val startsAt = grouping.key ?: return@forEach
            val posts = grouping.value
            section {
                filigree {
                    heading4(startsAt.toRelativeDayFormat(), SectionHeadingMods + OpacityMost)
                }
                olist {
                    posts.forEach {
                        listItem {
                            largePostCard(it)
                        }
                    }
                }
            }
        }
    }
}