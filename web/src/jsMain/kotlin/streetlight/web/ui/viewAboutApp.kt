package streetlight.web.ui

import koala.dom.RenderContext
import koala.dom.shellBox
import streetlight.web.shells.AboutKey
import streetlight.web.shells.aboutShell

fun RenderContext.viewAboutApp() {

    val root = shellBox(AboutKey.id) {
        aboutShell()
    }
}