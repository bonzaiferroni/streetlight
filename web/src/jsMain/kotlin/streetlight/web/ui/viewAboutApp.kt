package streetlight.web.ui

import koala.dom.ViewScope
import koala.dom.shellBox
import streetlight.web.shells.aboutShell

fun ViewScope.viewAboutApp() {
    shellBox {
        aboutShell()
    }
}