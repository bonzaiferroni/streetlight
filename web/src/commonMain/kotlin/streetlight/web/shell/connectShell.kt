package streetlight.web.shell

import koala.LottieFile
import koala.VideoFile
import koala.component.card
import koala.component.*
import koala.component.box
import koala.component.column
import koala.component.div
import koala.component.section
import koala.modifier.*
import koala.page.Id
import koala.page.setId
import kotlinx.css.pct
import kotlinx.html.FlowContent
import streetlight.model.data.ConnectType
import streetlight.web.component.connectPicker
import streetlight.web.layout.pageBody
import streetlight.web.layout.sourcePathShell
import streetlight.web.page.AppAttribute
import streetlight.web.page.appHeader
import streetlight.web.view.ConnectStyle

/** Renders the connect page, showing the panel of [type], or a prompt to choose one when it is `null`. */
fun FlowContent.connectShell(type: ConnectType?) {
    pageBody(sourcePathShell("connectShell.kt"), null, { appHeader(Height(10)) }) {
        setId(ConnectShell.RootId)
        setAttribute(AppAttribute.ConnectType, type)

        section(ContainerTypeInlineSize) {
            column(modify(ContainerMdRow, FlexItems1, AlignItemsCenter)) {
                column {
                    heading3("Connect to your community", AlignSelfCenter)
                    connectPicker(modify(Width(60.pct), AlignSelfCenter))
                }
                box {
                    video(
                        VideoFile.MapZoomClick,
                        modify(BorderRadius2, OverflowClip, Outline, MoonShadow)
                    )
                }
            }
        }

        div(ConnectStyle.Prompt) {
            textBlock("Choose who you are to see how Streetlight connects you.", modify(InkDimFg, TextAlignCenter))
        }

        ConnectType.entries.forEach { connectType ->
            div(modify(ConnectStyle.Panel, AppAttribute.ConnectOption.to(connectType))) {
                card(modify(ZenBg, Padding(4))) {
                    textBlock("${connectType.label}: content coming soon.")
                }
            }
        }
    }
}

object ConnectShell {
    val RootId = Id("connect-shell__root")
}
