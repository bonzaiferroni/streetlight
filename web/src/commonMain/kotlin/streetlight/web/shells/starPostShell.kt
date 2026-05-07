package streetlight.web.shells

import kabinet.utils.toAgoFormat
import kampfire.model.largest
import koala.css.AlignSelfCenter
import koala.css.AntiShadow
import koala.css.Aspect1
import koala.css.Aspect3By2
import koala.css.Bold
import koala.css.BorderRadius2
import koala.css.BorderRadius50P
import koala.css.Gap0
import koala.css.Height5
import koala.css.JustifyContentCenter
import koala.css.LargeText
import koala.css.LineHeight1
import koala.css.LineHeight115
import koala.css.MarginTop4
import koala.css.MaxWidthTextBody
import koala.css.MoonShadow
import koala.css.OpacityMost
import koala.css.OverflowClip
import koala.css.Padding0
import koala.css.Padding4
import koala.css.TextAlignCenter
import koala.css.ZenBg
import koala.css.modify
import koala.html.Id
import koala.html.card
import koala.html.column
import koala.html.dataIsland
import koala.html.featureImage
import koala.html.filigree
import koala.html.heading1
import koala.html.heading4
import koala.html.image
import koala.html.markdown
import koala.html.mount
import koala.html.row
import koala.html.section
import koala.html.textBlock
import kotlinx.html.FlowContent
import streetlight.model.data.StarPost
import streetlight.web.layouts.cellRow
import streetlight.web.layouts.postedAtCell
import streetlight.web.layouts.starCell
import streetlight.web.pages.appFooter

fun FlowContent.starPostShell(post: StarPost) {
    section(PostKey.ShellId) {
        column(modify(Gap0)) {
            heading1(post.title, modify(TextAlignCenter, AntiShadow, LineHeight115, MarginTop4))
            filigree {
                heading4("in Denver This Weekend", modify(OpacityMost, LineHeight115))
            }
        }

        card(modify(OverflowClip, Gap0, ZenBg, Padding0)) {
            post.images.largest?.let {
                featureImage(it, modify(Aspect3By2))
            }

            cellRow(listOf(
                { starCell(post.username, post.userThumb) },
                { postedAtCell(post.createdAt) }
            ))

            post.text?.let {
                column(modify(Padding4, AlignSelfCenter, MaxWidthTextBody, LargeText)) {
                    markdown(it)
                }
            }
        }

        mount(PostKey.TalkId)

        appFooter()

        dataIsland(PostKey.IslandId, post)
    }
}

object PostKey {
    val ShellId = Id("post-shell")
    val IslandId = Id("post-data")
    val TalkId = Id("post-talk")
}

