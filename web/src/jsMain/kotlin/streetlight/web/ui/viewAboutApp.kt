package streetlight.web.ui

import koala.dom.ScopedDOM
import koala.dom.shellBox
import streetlight.web.shells.AboutKey
import streetlight.web.shells.aboutShell

fun ScopedDOM.viewAboutApp() {

    val root = shellBox(AboutKey.id) {
        aboutShell()
    }
}