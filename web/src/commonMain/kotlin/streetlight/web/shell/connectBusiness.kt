package streetlight.web.shell

import koala.SvgFile
import koala.VideoFile
import koala.component.card
import koala.component.column
import koala.component.filigree
import koala.component.heading2
import koala.component.heading3
import koala.component.row
import koala.markdown.lazyMarkdown
import koala.markdown.markdown
import koala.modifier.Accent
import koala.modifier.AccentFg
import koala.modifier.AlignItemsCenter
import koala.modifier.AlignSelfCenter
import koala.modifier.BorderRadius2
import koala.modifier.ContainerMdRow
import koala.modifier.ContainerTypeInlineSize
import koala.modifier.FlexItems1
import koala.modifier.Gap
import koala.modifier.Gap2Px
import koala.modifier.MarginBottom
import koala.modifier.MarginTop
import koala.modifier.MaxWidth
import koala.modifier.MoonShadow
import koala.modifier.Outline
import koala.modifier.OverflowClip
import koala.modifier.Padding
import koala.modifier.PaddingX
import koala.modifier.Primary
import koala.modifier.PrimaryCardBg
import koala.modifier.PrimaryFg
import koala.modifier.Prose
import koala.modifier.SystemFg
import koala.modifier.TextAlignCenter
import koala.modifier.ValidFg
import koala.modifier.invoke
import koala.modifier.modify
import kotlinx.css.pct
import kotlinx.html.FlowContent
import streetlight.model.data.OnboardType
import streetlight.model.ui.HomeRoute
import streetlight.model.ui.OnboardRoute
import streetlight.web.layout.BodyStyle

fun FlowContent.connectBusiness() {
    column(modify(BodyStyle.ZenColumn, ContainerTypeInlineSize)) {
        column(MarginTop(4)) {
            filigree {
                heading2("Put your events on the map")
            }
            markdown(businessIntro, modify(MaxWidth(80.pct), AlignSelfCenter, PaddingX(2), Prose, TextAlignCenter))
        }

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

        connectSection("Choose your look", themeInfo) {
            videoBox(VideoFile.ThemeShowcase)
        }

        connectSection("Build your page", layoutInfo) {
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

private val businessIntro by lazyMarkdown("""
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
| **Free now, free later.** We are committed to an open and free platform. |
| Follow a guided tour that will get you started. | 
""")

private val supportPathInfo by lazyMarkdown("""
| We can build it for you. | 
|:---|
| **Scaled to meet your needs.** We'll work with you to find a solution that fits your business. |
| We'll connect you to a web developer from your area. |
""")

private val noAds = """
No ads, no paid prioritization. Events are listed in the order they are scheduled. Events can be shared within Streetlight and on other sites.
"""