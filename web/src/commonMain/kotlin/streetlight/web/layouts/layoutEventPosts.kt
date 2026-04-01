package streetlight.web.layouts

import kabinet.utils.toRelativeDayFormat
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.EventPost
import streetlight.web.shells.SectionHeadingMod

fun FlowContent.layoutEventPosts(posts: List<EventPost>) {
    column {
        heading2("Upcoming Events", SectionHeadingMod)
        val groupings = posts.groupBy { it.event?.startsAt }
        groupings.forEach { grouping ->
            val startsAt = grouping.key ?: return@forEach // td: show removed post content
            val posts = grouping.value
            section {
                filigree(modify(MarginTop2)) {
                    heading4(startsAt.toRelativeDayFormat(), SectionHeadingMod + OpacityMost)
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