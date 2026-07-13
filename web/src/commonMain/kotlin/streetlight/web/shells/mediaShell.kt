package streetlight.web.shells

import koala.css.AlignSelfCenter
import koala.css.AntiShadow
import koala.css.BorderRadius1
import koala.css.BorderRadius2
import koala.css.Gap0
import koala.css.JustifyContentEnd
import koala.css.TextLarge
import koala.css.LineHeight115
import koala.css.MarginTop4
import koala.css.MaxHeight64
import koala.css.MaxWidthTextBody
import koala.css.MoonShadow
import koala.css.OpacityHigh
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
import streetlight.model.data.Media
import streetlight.model.ui.MediaUpdateRoute
import streetlight.web.layouts.cellBlock
import streetlight.web.layouts.postedAtCell
import streetlight.web.layouts.starCell
import streetlight.web.pages.appFooter

fun FlowContent.mediaShell(media: Media) {
    section(PostKey.ShellId) {
        column(modify(Gap0)) {
            heading1(media.title, modify(TextAlignCenter, AntiShadow, LineHeight115, MarginTop4))
            filigree {
                heading4("in Denver This Weekend", modify(OpacityHigh, LineHeight115))
            }
        }

        media.image?.largest?.let {
            column(modify(SideBorder, BorderRadius1)) {
                image(it, modify(AlignSelfCenter, MaxHeight64, BorderRadius2, MoonShadow))
            }
        }

        card(modify(OverflowClip, Gap0, ZenBg, Padding0)) {
            cellBlock {
                starCell(media.username)
                postedAtCell(media.createdAt)
            }

            media.text?.let {
                column(modify(Padding2, AlignSelfCenter, MaxWidthTextBody, TextLarge)) {
                    markdown(it)
                }
            }
        }

        row(modify(JustifyContentEnd)) {
            btn("edit", MediaUpdateRoute(media.slug), modify(Zen))
        }

        mount(PostKey.TalkId, modify(MarginTop4))

        appFooter()

        dataIsland(PostKey.IslandId, media)
    }
}

object PostKey {
    val ShellId = Id("post-shell")
    val IslandId = Id("post-data")
    val TalkId = Id("post-talk")
}

