package streetlight.web.ui

import koala.dom.AppScope
import koala.dom.shellBox
import streetlight.web.shells.AboutKey
import streetlight.web.shells.aboutShell

fun AppScope.viewAboutApp() {

    val root = shellBox(AboutKey.id) {
        aboutShell()
    }
}