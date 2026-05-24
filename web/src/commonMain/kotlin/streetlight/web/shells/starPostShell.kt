package streetlight.web.shells

import kampfire.model.largest
import koala.css.AlignSelfCenter
import koala.css.AntiShadow
import koala.css.BorderRadius1
import koala.css.BorderRadius2
import koala.css.Gap0
import koala.css.JustifyContentEnd
import koala.css.LargeText
import koala.css.LineHeight115
import koala.css.MarginTop4
import koala.css.MaxHeight64
import koala.css.MaxWidthTextBody
import koala.css.MoonShadow
import koala.css.OpacityMost
import koala.css.OverflowClip
import koala.css.Padding0
import koala.css.Padding2
import koala.css.SideBorder
import koala.css.TextAlignCenter
import koala.css.Zen
import koala.css.ZenBg
import koala.css.modify
import koala.html.Id
import koala.html.btn
import koala.html.card
import koala.html.column
import koala.html.dataIsland
import koala.html.filigree
import koala.html.heading1
import koala.html.heading4
import koala.html.image
import koala.html.markdown
import koala.html.mount
import koala.html.row
import koala.html.section
import kotlinx.html.FlowContent
import streetlight.model.data.Post
import streetlight.web.PostUpdateRoute
import streetlight.web.layouts.cellRow
import streetlight.web.layouts.postedAtCell
import streetlight.web.layouts.starCell
import streetlight.web.pages.appFooter

fun FlowContent.postShell(post: Post) {
    section(PostKey.ShellId) {
        column(modify(Gap0)) {
            heading1(post.title, modify(TextAlignCenter, AntiShadow, LineHeight115, MarginTop4))
            filigree {
                heading4("in Denver This Weekend", modify(OpacityMost, LineHeight115))
            }
        }

        post.images.largest?.let {
            column(modify(SideBorder, BorderRadius1)) {
                image(it, modify(AlignSelfCenter, MaxHeight64, BorderRadius2, MoonShadow))
            }
        }

        card(modify(OverflowClip, Gap0, ZenBg, Padding0)) {
            cellRow(listOf(
                { starCell(post.username, post.userThumb) },
                { postedAtCell(post.createdAt) }
            ))

            post.text?.let {
                column(modify(Padding2, AlignSelfCenter, MaxWidthTextBody, LargeText)) {
                    markdown(it)
                }
            }
        }

        row(modify(JustifyContentEnd)) {
            btn("edit", PostUpdateRoute(post.slug), modify(Zen))
        }

        mount(PostKey.TalkId, modify(MarginTop4))

        appFooter()

        dataIsland(PostKey.IslandId, post)
    }
}

object PostKey {
    val ShellId = Id("post-shell")
    val IslandId = Id("post-data")
    val TalkId = Id("post-talk")
}

