package streetlight.web.layouts

import kabinet.utils.toRelativeDayFormat
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.EventPost
import streetlight.model.utils.toLocalDateTime
import streetlight.web.shells.SectionHeadingMod

fun FlowContent.layoutEventPosts(
    headingText: String?,
    posts: List<EventPost>,
) {
    column {
        headingText?.let {
            heading2(headingText, SectionHeadingMod)
        }
        val groupings = posts.groupBy { it.event?.startsAt?.toLocalDateTime()?.date }
        groupings.forEach { grouping ->
            val date = grouping.key ?: return@forEach // td: show removed post content
            val posts = grouping.value
            val dayFormat = posts.first().event!!.startsAt.toRelativeDayFormat()
            section {
                filigree(modify(MarginTop2)) {
                    heading4(dayFormat, SectionHeadingMod + OpacityHigh)
                }
                olist {
                    posts.forEach {
                        listItem {
                            largeEventPostCard(it)
                        }
                    }
                }
            }
        }
    }
}