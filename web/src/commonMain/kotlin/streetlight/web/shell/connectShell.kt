package streetlight.web.shell

import koala.SvgFile
import koala.SvgPack
import koala.Video
import koala.VideoFile
import koala.component.card
import koala.component.*
import koala.component.box
import koala.component.column
import koala.component.div
import koala.component.section
import koala.markdown.lazyMarkdown
import koala.markdown.markdown
import koala.modifier.*
import koala.page.Id
import koala.page.setId
import kotlinx.css.pct
import kotlinx.html.FlowContent
import streetlight.model.data.OnboardType
import streetlight.model.ui.OnboardRoute
import streetlight.web.component.connectPicker
import streetlight.web.layout.BodyStyle
import streetlight.web.layout.pageBody
import streetlight.web.layout.sourcePathShell
import streetlight.web.page.AppAttribute
import streetlight.web.page.appHeader
import streetlight.web.view.ConnectStyle

/** Renders the connect page, showing the panel of [initialType]. */
fun FlowContent.connectShell(initialType: OnboardType) {
    pageBody(sourcePathShell("connectShell.kt"), null, { appHeader(Height(10)) }) {
        setId(ConnectShell.RootId)
        setAttribute(AppAttribute.OnboardType, initialType)

        section(modify(ContainerTypeInlineSize, MarginTop(2))) {
            column(modify(ContainerMdRow, FlexItems1, TextAlignCenter)) {
                column {
                    heading4("Connect to your community.")
                    connectPicker(modify(Width(80.pct), AlignSelfCenter))
                    textBlock("Find out what Streetlight can offer.", modify(Italic, Padding(2)))
                }
                videoBox(VideoFile.MapZoomClick)
            }
        }

        OnboardType.entries.forEach { onboardType ->
            column(modify(ConnectStyle.Panel, BodyStyle.MainColumn, AppAttribute.ConnectOption.to(onboardType), FadeIn)) {
                when (onboardType) {
                    OnboardType.BusinessOwner -> connectBusiness()
                    OnboardType.Performer, OnboardType.Developer, OnboardType.EventHost -> card(modify(ZenBg, Padding(4))) {
                        textBlock("${onboardType.label}: content coming soon.")
                    }
                }
            }
        }
    }
}

fun FlowContent.connectBusiness() {
    column(modify(BodyStyle.MainColumn, ContainerTypeInlineSize)) {
        markdown(businessIntro, modify(MaxWidth(80.pct), AlignSelfCenter, PaddingX2, Prose, TextAlignCenter))

        column(modify(ContainerMdRow, FlexItems1, Gap2Px, BorderRadius2, OverflowClip, Outline)) {
            iconCard(
                SvgFile.MapPinShare,
                "Get on the map",
                "Our interactive map shows everything that is happening in your neighborhood."
            )
            iconCard(
                SvgFile.CalendarRepeat,
                "Automatically share events",
                "Share your a link to your calendar page and we will read it once a day and put your events on the map."
            )
            iconCard(
                SvgFile.Broadcast,
                "A thing",
                "I bet you never knew this."
            )
            iconCard(SvgFile.Share, "another thing", "Exactly what you are looking for.")
        }

        row(modify(AlignItemsCenter, FlexItems1)) {
            markdown(themeInfo)
            videoBox(VideoFile.ThemeShowcase)
        }
        row(modify(AlignItemsCenter, FlexItems1)) {
            markdown(layoutInfo)
            videoBox(VideoFile.LayoutShowcase)
        }
        btn("get started", OnboardRoute(OnboardType.BusinessOwner), Zen)
    }
}

private fun FlowContent.videoBox(video: Video) {
    box(modify(BorderRadius2, OverflowClip, Outline, MoonShadow)) {
        video(video)
    }
}

private fun FlowContent.iconCard(svg: SvgPack, title: String, text: String) {
    column(modify(AlignItemsCenter, TextAlignCenter, Padding(2), ZenBg)) {
        icon(svg.animated, modify(AccentFg, Width(8)))
        heading6(title, modify(OpacityHigh, Height(5)))
        textBlock(text)
    }
}

object ConnectShell {
    val RootId = Id("connect-shell__root")
}

private val businessIntro by lazyMarkdown("""
### Put your events on the map ---

*Streetlight is built upon the idea that the fullest human experience begins shoulder to shoulder, face to face, in a warm and friendly place. You've worked hard to create a place for people to gather, and we've made it easy for people to discover what is happening there.* 
""")

private val themeInfo by lazyMarkdown("""
Create a page on Streetlight with a customized look. Choose your colors and their presentation within our theme editor. 
""")

private val layoutInfo by lazyMarkdown("""
Create a layout that features your events or custom content. 

Need a homepage for your business? Host your content with your own custom branding by creating a free subdomain, *mybusiness.streetlight.ing*. 

Your content integrates seamlessly with Streetlight, and visitors to your subdomain see only the content you create.
""")