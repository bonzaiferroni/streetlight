package streetlight.web.shell

import koala.SvgPack
import koala.Video
import koala.VideoFile
import koala.component.card
import koala.component.*
import koala.component.box
import koala.component.column
import koala.component.section
import koala.markdown.ParsedBlock
import koala.markdown.markdown
import koala.model.WebRoute
import koala.modifier.*
import koala.page.Id
import koala.page.setId
import kotlinx.css.pct
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import streetlight.model.data.OnboardType
import streetlight.web.component.connectPicker
import streetlight.web.layout.BodyStyle
import streetlight.web.layout.pageBody
import streetlight.web.layout.sourcePathShell
import streetlight.web.page.AppAttribute
import streetlight.model.ui.TopBar
import streetlight.web.page.topBar
import streetlight.web.view.ConnectStyle

/** Renders the connect page, showing the panel of [initialType]. */
fun FlowContent.connectShell(initialType: OnboardType) {
    pageBody(sourcePathShell("connectShell.kt"), null, { topBar(TopBar.Logo) }) {
        setId(ConnectShell.RootId)
        setAttribute(AppAttribute.OnboardType, initialType)

        section(modify(ContainerTypeInlineSize, MarginTop(2))) {
            column(modify(ContainerMdRow, FlexItems1, TextAlignCenter)) {
                column {
                    heading4("Connect to your community.")
                    connectPicker(modify(Width(75.pct), AlignSelfCenter))
                    textBlock("Find out what Streetlight can offer.", modify(Italic, Padding(1), OpacityHigh))
                }
                videoBox(VideoFile.MapZoomClick)
            }
        }

        OnboardType.entries.forEach { onboardType ->
            column(modify(ConnectStyle.Panel, BodyStyle.MainColumn, AppAttribute.ConnectOption.to(onboardType), FadeIn)) {
                when (onboardType) {
                    OnboardType.BusinessOwner -> connectBusiness()
                    OnboardType.Performer -> connectPerformer()
                    OnboardType.Developer, OnboardType.EventHost -> card(modify(ZenBg, Padding(4))) {
                        textBlock("${onboardType.label}: content coming soon.")
                    }
                }
            }
        }
    }
}

fun FlowContent.connectSection(
    heading: String?,
    markdown: List<ParsedBlock>,
    content: DIV.() -> Unit = { box() },
) {
    column(modify(ContainerMdRow, AlignItemsCenter, FlexItems1)) {
        column(Padding(2)) {
            heading?.let {
                heading3(it, TextAlignCenter)
            }
            markdown(markdown)
        }
        content()
    }
}

fun FlowContent.videoBox(video: Video) {
    box(modify(BorderRadius2, OverflowClip, Outline, MoonShadow)) {
        video(video)
    }
}

fun FlowContent.iconCard(svg: SvgPack, title: String, text: String, colorMod: Class) {
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

fun FlowContent.actionCard(svg: SvgPack, buttonLabel: String, buttonClass: Class, route: WebRoute, description: List<ParsedBlock>) {
    column {
        icon(svg.animated, modify(AlignSelfCenter, Width(8)))
        btn(buttonLabel, route, modify(buttonClass, AlignSelfCenter))
        markdown(description, MarginTop(3))
    }
}

object ConnectShell {
    val RootId = Id("connect-shell__root")
}

