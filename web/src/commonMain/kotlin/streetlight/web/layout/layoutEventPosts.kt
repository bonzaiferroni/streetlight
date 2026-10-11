package streetlight.web.layout

import kabinet.utils.toFutureFormat
import koala.modifier.*
import kotlinx.html.FlowContent
import streetlight.model.data.EventPost
import streetlight.model.utils.toLocalDateTime
import streetlight.web.shell.SectionHeadingMod
import koala.component.*
import koala.layout.*

fun FlowContent.layoutEventPosts(
    headingText: String?,
    posts: List<EventPost>,
) {
    column {
        headingText?.let {
            heading2(headingText, SectionHeadingMod)
        }
        val groupings = posts.groupBy { it.event.startsAt?.toLocalDateTime()?.date }
        groupings.forEach { grouping ->
            // val date = grouping.key ?: return@forEach // td: show removed post content
            val posts = grouping.value
            val dayFormat = posts.first().event.startsAt?.toFutureFormat() ?: return@forEach
            section {
                filigree(MarginTop(2)) {
                    heading4(dayFormat, modify(SectionHeadingMod, OpacityHigh))
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