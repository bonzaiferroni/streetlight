package streetlight.web.ui

import koala.SiteImage
import koala.dom.*
import streetlight.web.model.FrontDesk

fun ViewScope.viewFrontDesk(model: FrontDesk) {
    column(BodyStyle.Mod) {
        featureHeader("Help & Feedback", "The Front Desk", SiteImage.FrontDesk)

        tabs {
            tab("Help") {
                textBlock("yer help")
            }
            tab("Feedback") {
                column {
                    textField(model.textField, "feedback")
                    button("send", model::sendFeedback)

                    flowBlock(model.feedFlow) { feed ->
                        feed.forEach { feedback ->
                            textBlock(feedback.text)
                        }
                    }
                }
            }
            tab("Report a bug") {
                textBlock("yer bug report")
            }
        }
    }
}

fun RouteScope.viewFrontDeskRoute() {
    val model = app.getFeedbackDesk(contentScope)
    viewFrontDesk(model)
}