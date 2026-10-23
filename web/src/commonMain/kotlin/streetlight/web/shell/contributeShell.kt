package streetlight.web.shell

import kampfire.model.toUrl
import koala.SiteImage
import koala.SvgFile
import koala.SvgPack
import koala.component.*
import koala.modifier.*
import kotlinx.css.pct
import kotlinx.html.FlowContent
import streetlight.model.ui.FrontDeskRoute
import streetlight.model.ui.HomeRoute
import streetlight.web.component.connectPicker
import streetlight.web.layout.mainBody
import streetlight.web.layout.pageHeader

/** The contribute page: the connect picker, then the ways anyone can help Streetlight grow. */
fun FlowContent.contributeShell() {
    mainBody("contributeShell.kt") {
        pageHeader("Contribute", "help Streetlight grow", SiteImage.crossing)

        column(modify(AlignItemsCenter, TextAlignCenter)) {
            heading4("Find your place.")
            connectPicker(modify(Width(80.pct), MaxWidth(64)))
            textBlock(
                "Business owners, performers, developers and event hosts each have a path in.",
                modify(Italic, Padding(1), OpacityHigh)
            )
        }

        section(ContainerTypeInlineSize) {
            column(modify(ContainerMdRow, FlexItems1, Gap(2))) {
                contributeCard(SvgFile.MessageDots, "Share feedback", "Tell us what works, what doesn't, and what you wish Streetlight could do.") {
                    btn("front desk", FrontDeskRoute, Zen)
                }
                contributeCard(SvgFile.Github, "Work on the code", "Streetlight is free and open source, written in Kotlin. Contributors at every stage are welcome.") {
                    btn("GitHub", "https://github.com/bonzaiferroni/streetlight".toUrl(), Zen)
                }
                contributeCard(SvgFile.Share, "Spread the word", "Share an event, invite a venue, or tell a friend what's happening nearby.") {
                    btn("explore", HomeRoute(), Zen)
                }
            }
        }
    }
}

/** A card of one way to help: its icon, [title], [text], and the [action] that starts it. */
private fun FlowContent.contributeCard(svg: SvgPack, title: String, text: String, action: FlowContent.() -> Unit) {
    card(modify(ZenBg, Gap(2), AlignItemsCenter, TextAlignCenter)) {
        icon(svg.animated, Width(8))
        heading5(title)
        textBlock(text, modify(Flex1, OpacityHigh))
        action()
    }
}
