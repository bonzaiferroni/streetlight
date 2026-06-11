package streetlight.web.ui

import koala.dom.DOMRender
import koala.dom.shellBox
import streetlight.web.shells.AboutKey
import streetlight.web.shells.aboutShell

fun DOMRender.viewAboutApp() {

    val root = shellBox(AboutKey.id) {
        aboutShell()
    }
}