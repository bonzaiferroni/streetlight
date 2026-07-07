package streetlight.web.ui

import koala.SiteImage
import koala.dom.AppScope
import koala.dom.button
import koala.dom.column
import koala.dom.flowBlock
import koala.dom.textBlock
import koala.dom.textField
import streetlight.web.model.FrontDesk

fun AppScope.viewFrontDesk(model: FrontDesk) {
    column(Body.mod) {
        featureHeader("Help & Support", "The Front Desk", SiteImage.FrontDesk.url)

        textField("feedback", model::setText, model.textFlow)
        button("send", model::sendFeedback)

        flowBlock(model.feedFlow) { feed ->
            feed.forEach { feedback ->
                textBlock(feedback.text)
            }
        }
    }
}

fun AppScope.viewFrontDeskRoute() {
    val model = app.getFeedbackDesk(parentScope)
    viewFrontDesk(model)
}