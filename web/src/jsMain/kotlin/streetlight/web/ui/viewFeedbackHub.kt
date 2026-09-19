package streetlight.web.ui

import koala.dom.*
import koala.html.heading4
import koala.modifier.*
import streetlight.web.model.FeedbackHub

fun ViewScope.viewFeedbackHub() {
    val model = FeedbackHub(contentScope, api, toaster)
    column(MarginTop1) {
        filigree {
            heading4("Share Feedback")
        }

        column {
            centeredText("How is Streetlight working out for you? Is there anything that needs fixed?")
            row(JustifyContentEnd) {
                blockLabel("type") {
                    dropMenu(model.feedbackTypeState)
                }
                blockLabel("sharing") {
                    dropMenu("Share Privately", "Share Publicly", model.isPrivateState)
                }
            }
            markdownEditor(model.textState, "feedback", MinHeight(32))
            formSubmit("Send", model::sendFeedback)
        }

        filigree {
            heading4("Public Feedback")
        }

        flowBlock(model.feedFlow, FlexColumn) { feed ->
            feed.forEach { feedback ->
                markdown(feedback.text)
            }
        }
    }
}