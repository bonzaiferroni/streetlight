package streetlight.web.shell

import koala.LottieFile
import koala.layout.card
import koala.component.heading2
import koala.component.lottie
import koala.component.textBlock
import koala.layout.column
import koala.layout.div
import koala.layout.section
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
                column(AlignItemsCenter) {
                    lottie(LottieFile.StreetlightNight, modify(Width(100.pct), MaxWidth(64)))
                    heading2("Connect to your community", SectionHeadingMod)
                }
                connectPicker()
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
