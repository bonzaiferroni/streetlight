package streetlight.web.ui

import koala.dom.*
import kotlinx.html.js.p

fun AppScope.viewSandbox() {
    renderTabs()
}

fun AppScope.renderTabs() {
    tabs {
        (0..20).forEach {
            tab("tab $it") {
                textBlock("tab $it content")
            }
        }
    }
}

fun AppScope.myContent() {
    // fine, append hasn't completed yet
    p { +"yer UI" }
    launchEffect {
        // now disallowed
        // p { +"more UI" }

        launch {
        }
    }
}


