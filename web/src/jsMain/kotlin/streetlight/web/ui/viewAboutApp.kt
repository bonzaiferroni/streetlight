package streetlight.web.ui

import koala.dom.ViewScope
import koala.dom.shellBox
import streetlight.web.shells.AboutKey
import streetlight.web.shells.aboutShell

fun ViewScope.viewAboutApp() {

    val root = shellBox(AboutKey.id) {
        aboutShell()
    }
}