package streetlight.web.ui

import koala.dom.ViewContext
import koala.dom.shellBox
import streetlight.web.model.Streetlight
import streetlight.web.shells.AboutKey
import streetlight.web.shells.aboutShell

fun ViewContext<Streetlight>.viewAboutApp() {
    val app = model

    val root = shellBox(AboutKey.id) {
        aboutShell()
    }
}