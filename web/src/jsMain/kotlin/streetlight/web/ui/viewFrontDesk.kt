package streetlight.web.ui

import koala.SiteImage
import koala.dom.*

fun ViewScope.viewFrontDesk() {
    column(BodyStyle.MainColumn) {
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