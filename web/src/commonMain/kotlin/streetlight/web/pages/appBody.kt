package streetlight.web.pages

import koala.JsFile
import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.HTML
import kotlinx.html.body
import streetlight.web.HomeRoute

fun HTML.appBody(
    block: (DIV.() -> Unit)? = null
) {
    body {
        appOverlay()
        div(AppBodyKey.ViewportId) {
            div(AppBodyKey.SpacerLeftId)
            column(AppBodyKey.AppPanelId) {
                appHeader()
                box(AppBodyKey.ContentBoxId) {
                    box(AppBodyKey.PortalMountId)
                    box(id = AppBodyKey.ShellBoxId, block = block)
                }
            }
            div(AppBodyKey.SpacerRightId) {
                column {
                    spacer(modify(AppHeaderKey.Height))
                    column(AppBodyKey.PanelRightId) {
                        textBlock("yer panel")
                    }
                    spacer(modify(AppHeaderKey.Height))
                }
            }
        }
        linkScript(JsFile.Web)
    }
}

object AppBodyKey {
    val ViewportId = Id("viewport-box")
    val AppPanelId = Id("app-box")
    val PortalMountId = Id("portal-mount")
    val ShellBoxId = Id("shell-box")
    val ContentBoxId = Id("content-box")
    val SpacerLeftId = Id("spacer-left")
    val SpacerRightId = Id("spacer-right")
    val PanelLeftId = Id("panel-left")
    val PanelRightId = Id("panel-right")
}

// language="CSS"
val AppBodyCss = """
${AppBodyKey.ViewportId} {
    width: 100vw;
    height: 100dvh;
    display: flex;
    justify-content: center;
}

${AppBodyKey.AppPanelId} {
    min-height: 100dvh;
    max-width: var(--body-width);
    padding: var(--unit-spacing);
}

${AppBodyKey.AppPanelId} {
    width: 100%;
    display: grid;
}

${AppBodyKey.PortalMountId},
${AppBodyKey.ShellBoxId} {
    width: 100%;
    grid-area: 1 / 1;
    min-width: 0;
}

${AppBodyKey.PortalMountId} > *,
${AppBodyKey.ShellBoxId} > * {
    width: 100%;
}

${AppBodyKey.SpacerLeftId},
${AppBodyKey.SpacerRightId} {
    display: none;
}

${AppBodyKey.SpacerLeftId}$Reveal,
${AppBodyKey.SpacerRightId}$Reveal {
    display: block;
    width: 300px;
}

${AppBodyKey.SpacerLeftId} > *,
${AppBodyKey.SpacerRightId} > * {
    position: fixed;
    top: 0;
    width: inherit;
}

/* content box */
#content-box {
    opacity: 0;
    transition: opacity var(--magic-interval) var(--magic-easing);
}

#content-box.reveal {
    opacity: 1;
}
"""

