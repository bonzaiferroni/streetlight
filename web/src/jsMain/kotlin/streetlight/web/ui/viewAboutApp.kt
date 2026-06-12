package streetlight.web.ui

import koala.dom.RenderScope
import koala.dom.shellBox
import streetlight.web.shells.AboutKey
import streetlight.web.shells.aboutShell

fun RenderScope.viewAboutApp() {

    val root = shellBox(AboutKey.id) {
        aboutShell()
    }
}