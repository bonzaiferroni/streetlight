package streetlight.web.view

import koala.dom.*
import koala.component.heading4
import koala.modifier.*
import streetlight.web.model.FeedbackHub
import koala.dom.component.*
import koala.dom.layout.*
import koala.dom.markdown.*
import koala.model.*
import streetlight.web.model.api
import streetlight.web.dom.layout.centeredText
import streetlight.web.dom.layout.formSubmit
import streetlight.web.model.toaster

fun ViewScope.viewFeedbackHub() {
    val model = FeedbackHub(contentScope, api.feedback, toaster)
    val messenger = MessageStore()
    column(MarginTop(1)) {
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
            formSubmit("Send", { model.sendFeedback(messenger) }, messenger)
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