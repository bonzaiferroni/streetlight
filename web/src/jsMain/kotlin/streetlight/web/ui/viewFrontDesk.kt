package streetlight.web.ui

import koala.SiteImage
import koala.dom.*
import streetlight.web.model.FeedbackHub

fun ViewScope.viewFrontDesk() {
    column(BodyStyle.MainColumn) {
        featureHeader("Help & Feedback", "The Front Desk", SiteImage.FrontDesk)

        lazyTabs {
            tab("Help") {
                textBlock("yer help")
            }
            tab("Feedback") {
                viewFeedbackHub()
            }
            tab("Report a bug") {
                textBlock("yer bug report")
            }
        }
    }
}

fun RouteScope.viewFrontDeskRoute() {
    viewFrontDesk()
}