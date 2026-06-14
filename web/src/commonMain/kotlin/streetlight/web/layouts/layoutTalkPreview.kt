package streetlight.web.layouts

import kabinet.utils.toAgoFormat
import kampfire.api.toSlug
import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.Comment
import streetlight.model.data.CommentId
import streetlight.web.StarRoute
import streetlight.web.shells.SectionHeadingMod

fun FlowContent.layoutTalkPreview(route: AppRoute, comments: List<Comment>) {
    val comments = comments.sortedBy { it.createdAt }
    val childIds = mutableListOf<CommentId>()

    val controlMod = modify(BorderRadius1, FlexDirectionRow, AlignItemsCenter)

    fun FlowContent.layoutComment(comment: Comment) {
        if (childIds.contains(comment.commentId)) return
        val children = comments.filter { it.parentId == comment.commentId }

        column {
            row(modify(AlignItemsCenter, modify(Height5))) {
                navigationIfNotNull(comment.username?.let { StarRoute(it)}) {
                    row(modify(AlignItemsCenter)) {
                        image(comment.thumb, modify(Aspect1))
                        heading5(comment.username?.value ?: "[Former Guest]")
                    }
                }
                textBlock(comment.createdAt.toAgoFormat())
                row(modify(JustifySelfEnd, AlignItemsCenter)) {
                    card(controlMod) {
                        textBlock(comment.lightCount.toString())
                        icon(SvgFile.Flame)
                    }

                    card(controlMod) {
                        textBlock(comment.replyCount.toString())
                        icon(SvgFile.MessagePlus)
                    }
                }
            }
            textBlock(comment.text)

            if (children.isNotEmpty()) {
                column(modify(PaddingLeft3)) {
                    children.forEach {
                        childIds.add(it.commentId)
                        layoutComment(comment)
                    }
                }
            }
        }
    }

    section {
        filigree { heading2("Galaxy Talk", SectionHeadingMod) }
        comments.forEach {
            layoutComment(it)
        }
        navigation(route) {
            column(modify(AlignItemsCenter, JustifyContentCenter, Height12)) {
                textBlock("No comments here yet, be the first.", modify(OpacityHigh))
            }
        }
    }
}