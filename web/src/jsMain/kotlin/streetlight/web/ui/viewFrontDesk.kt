package streetlight.web.ui

import koala.SiteImage
import koala.dom.*
import streetlight.web.model.FrontDesk

fun AppScope.viewFrontDesk(model: FrontDesk) {
    column(BodyStyle.Mod) {
        featureHeader("Help & Feedback", "The Front Desk", SiteImage.FrontDesk.url)

        tabs {
            tab("Help") {
                textBlock("yer help")
            }
            tab("Feedback") {
                column {
                    textField("feedback", model::setText, model.textFlow)
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

fun AppScope.viewFrontDeskRoute() {
    val model = app.getFeedbackDesk(parentScope)
    viewFrontDesk(model)
}