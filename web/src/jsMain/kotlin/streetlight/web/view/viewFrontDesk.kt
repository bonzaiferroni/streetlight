package streetlight.web.view

import koala.SiteImage
import koala.dom.*
import koala.dom.component.*
import koala.dom.layout.*
import streetlight.web.dom.layout.mainBody
import streetlight.web.layout.mainBody
import streetlight.web.layout.pageHeader

fun ViewScope.viewFrontDesk() {
    mainBody("viewFrontDesk.kt") {
        pageHeader("Help & Feedback", "The Front Desk", SiteImage.FrontDesk)

        lazyTabs {
            tab("Help") {
                textBlock("yer help")
            }
            tab("Feedback") {
                viewFeedbackHub()
            }
            tab("Report a bug") {
                viewBugReporter()
            }
        }
    }
}

fun RouteScope.viewFrontDeskRoute() {
    viewFrontDesk()
}