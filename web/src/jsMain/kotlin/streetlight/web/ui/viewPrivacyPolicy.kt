package streetlight.web.ui

import koala.dom.ViewContext
import koala.dom.shellBox
import streetlight.web.model.Streetlight
import streetlight.web.shells.AboutKey
import streetlight.web.shells.PrivacyPolicyKey
import streetlight.web.shells.aboutShell
import streetlight.web.shells.privacyPolicyShell

fun ViewContext<Streetlight>.viewPrivacyPolicy() {
    val app = model

    val root = shellBox(PrivacyPolicyKey.id) {
        privacyPolicyShell()
    }
}