package streetlight.web.view

import koala.dom.ViewScope
import koala.dom.page.shellBox
import streetlight.web.shell.aboutShell

fun ViewScope.viewAboutApp() {
    shellBox {
        aboutShell()
    }
}