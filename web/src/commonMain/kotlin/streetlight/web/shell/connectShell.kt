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
import koala.markdown.ParsedBlock
import koala.markdown.lazyMarkdown
import koala.markdown.markdown
import koala.model.AppRoute
import koala.modifier.*
import koala.modifier.AccentFg
import koala.page.Id
import koala.page.setId
import kotlinx.css.pct
import kotlinx.html.FlowContent
import streetlight.model.data.OnboardType
import streetlight.model.ui.HomeRoute
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
                    textBlock("Find out what Streetlight can offer.", modify(Italic, Padding(1), OpacityHigh))
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
    column(modify(BodyStyle.ZenColumn, ContainerTypeInlineSize)) {
        markdown(businessIntro, modify(MaxWidth(80.pct), AlignSelfCenter, PaddingX2, Prose, TextAlignCenter))

        column(modify(ContainerMdRow, FlexItems1, Gap2Px, BorderRadius2, OverflowClip, Outline)) {
            row(modify(FlexItems1, Gap2Px)) {
                iconCard(
                    SvgFile.MapPinShare,
                    "Get on the map",
                    "Our interactive map shows everything that is happening in your neighborhood.",
                    SystemFg,
                )
                iconCard(
                    SvgFile.CalendarRepeat,
                    "Automatically share events",
                    "We will read an existing calendar page once a day and share your events.",
                    AccentFg,
                )
            }
            row(modify(FlexItems1, Gap2Px)) {
                iconCard(
                    SvgFile.Home,
                    "Web hosting and more",
                    "Point users to an existing website or build one with our simple tools.",
                    PrimaryFg,
                )
                iconCard(
                    SvgFile.Seedling,
                    "Evolving platform",
                    "We are just getting started, share your ideas and shape the direction of Streetlight.",
                    ValidFg
                )
            }
        }

        column(modify(ContainerMdRow, AlignItemsCenter, FlexItems1)) {
            column(Padding(2)) {
                heading3("Choose your look", TextAlignCenter)
                markdown(themeInfo)
            }
            videoBox(VideoFile.ThemeShowcase)
        }
        column(modify(ContainerMdRow, AlignItemsCenter, FlexItems1)) {
            column(Padding(2)) {
                heading3("Build your page", TextAlignCenter)
                markdown(layoutInfo)
            }
            videoBox(VideoFile.LayoutShowcase)
        }

        card(modify(PrimaryCardBg, Gap(2), MarginBottom(4), Outline, MoonShadow)) {
            filigree {
                heading3("Choose your path")
            }

            row(modify(FlexItems1, Gap(2), Padding(1))) {
                actionCard(SvgFile.Trekking, "get started", Primary, OnboardRoute(OnboardType.BusinessOwner), selfPathInfo)
                actionCard(SvgFile.Code, "get support", Accent, HomeRoute(), supportPathInfo)
            }
        }
    }
}

private fun FlowContent.videoBox(video: Video) {
    box(modify(BorderRadius2, OverflowClip, Outline, MoonShadow)) {
        video(video)
    }
}

private fun FlowContent.iconCard(svg: SvgPack, title: String, text: String, colorMod: Class) {
    column(modify(AlignItemsCenter, TextAlignCenter, Gap(2), Padding(2), ZenBg)) {
        box(modify(ConnectStyle.IconBadge, PlaceItemsCenter, Width(16), Aspect1, colorMod)) {
            icon(svg.animated, modify(Width(8)))
        }
        box(modify(Height(4), PlaceItemsCenter)) {
            heading6(title)
        }
        textBlock(text, modify(OpacityHigh))
    }
}

private fun FlowContent.actionCard(svg: SvgPack, buttonLabel: String, buttonClass: Class, route: AppRoute, description: List<ParsedBlock>) {
    column {
        icon(svg.animated, modify(AlignSelfCenter, Width(8)))
        btn(buttonLabel, route, modify(buttonClass, AlignSelfCenter))
        markdown(description, MarginTop(3))
    }
}

object ConnectShell {
    val RootId = Id("connect-shell__root")
}

private val businessIntro by lazyMarkdown("""
### Put your events on the map ---

*Streetlight is built upon the idea that the fullest human experience begins shoulder to shoulder, face to face, in a warm and friendly place. You've worked hard to create a place for people to gather, and we've made it easy for people to discover.* 
""")

private val themeInfo by lazyMarkdown("""
Create a page on Streetlight with a customized look. Choose your colors and presentation within our theme editor. 
""")

private val layoutInfo by lazyMarkdown("""
Need a homepage for your business? Host your content with your own custom branding by creating a free subdomain, *mybusiness.streetlight.ing*. 

Your content integrates seamlessly with Streetlight, and visitors to your subdomain see only the content you create.
""")

private val selfPathInfo by lazyMarkdown("""
| Add your business to Streetlight. |
|:---|
| We've created a guided tour that will get you started. | 
| **Free now, free later.** We are committed to an open and free platform. |
""")

private val supportPathInfo by lazyMarkdown("""
| We can build it for you. | 
|:---|
| We'll connect you to a web developer from your area. |
| **Scaled to meet your needs.** We'll work with you to find a solution that fits your business. |
""")

private val noAds = """
No ads, no paid prioritization. Events are listed in the order they are scheduled. Events can be shared within Streetlight and on other sites.
"""