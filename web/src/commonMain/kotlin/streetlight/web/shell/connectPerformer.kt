package streetlight.web.shell

import koala.component.column
import koala.component.filigree
import koala.component.heading2
import koala.markdown.lazyMarkdown
import koala.markdown.markdown
import koala.modifier.AlignSelfCenter
import koala.modifier.ContainerTypeInlineSize
import koala.modifier.MarginTop
import koala.modifier.MaxWidth
import koala.modifier.PaddingX
import koala.modifier.Prose
import koala.modifier.TextAlignCenter
import koala.modifier.invoke
import koala.modifier.modify
import kotlinx.css.pct
import kotlinx.html.FlowContent
import streetlight.web.layout.BodyStyle

fun FlowContent.connectPerformer() {
    column(modify(BodyStyle.ZenColumn, ContainerTypeInlineSize)) {
        column(MarginTop(4)) {
            filigree {
                heading2("Share your music")
            }
            markdown(performerIntro, modify(MaxWidth(80.pct), AlignSelfCenter, PaddingX(2), Prose, TextAlignCenter))
        }

        // cards

        connectSection(null, comingUp)
    }
}

private val performerIntro by lazyMarkdown("""
Maybe you just picked up an instrument a couple months ago, maybe you've been playing for years. Music is a journey that wants to be shared, not just across wires but in a place where it can be heard and seen and felt.    
""")

private val comingUp by lazyMarkdown("""
Streetlight is still under construction, here is what we are building:

* Share your music online
* Find places to play
* Collaborate with other musicians
* Connect with venues and event coordinators
""")