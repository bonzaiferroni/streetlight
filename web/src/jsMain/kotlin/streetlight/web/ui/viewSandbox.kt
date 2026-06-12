package streetlight.web.ui

import koala.dom.*
import kotlinx.html.js.p

fun AppScope.viewSandbox() {
    renderTabs()
}

fun AppScope.renderTabs() {
    tabs {
        tab("one") {
            appendTabs()
        }
        tab("two") {
            textBlock("the second tab content")
        }
        tab("three") {
            textBlock("the third tab content")
        }
    }
}

fun TagScope.appendTabs() {
    tabs {
        tab("one") {
            textBlock("the first subtab content")
        }
        tab("two") {
            textBlock("the second subtab content")
        }
        tab("three") {
            textBlock("the third subtab content")
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


