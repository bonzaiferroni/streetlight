package streetlight.web.shell

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
import streetlight.model.data.OnboardType
import streetlight.model.ui.OnboardRoute
import streetlight.web.component.connectPicker
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

        OnboardType.entries.forEach { onboardType ->
            div(modify(ConnectStyle.Panel, AppAttribute.ConnectOption.to(onboardType))) {
                card(modify(ZenBg, Padding(4))) {
                    textBlock("${onboardType.label}: content coming soon.")
                }
                btn("get started", OnboardRoute(onboardType))
            }
        }
    }
}

object ConnectShell {
    val RootId = Id("connect-shell__root")
}
