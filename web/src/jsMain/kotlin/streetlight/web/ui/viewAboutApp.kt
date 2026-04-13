package streetlight.web.ui

import koala.css.Width100P
import koala.css.modify
import koala.dom.ViewContext
import koala.dom.shellBox
import streetlight.web.model.Streetlight
import streetlight.web.shells.AboutAppKey
import streetlight.web.shells.EventProfileKey
import streetlight.web.shells.aboutAppShell
import streetlight.web.shells.eventProfileShell

fun ViewContext<Streetlight>.viewAboutApp() {
    val app = model

    val root = shellBox(AboutAppKey.id) {
        aboutAppShell()
    }
}