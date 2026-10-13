package streetlight.web.view

import koala.dom.*
import koala.component.heading4
import koala.modifier.*
import streetlight.model.ui.Screen
import streetlight.web.model.BugReporter
import koala.dom.component.*
import koala.dom.markdown.*
import koala.model.*
import streetlight.web.model.api
import streetlight.web.dom.layout.centeredText
import streetlight.web.dom.layout.formSubmit
import streetlight.web.model.portal

fun ViewScope.viewBugReporter() {
    val model = BugReporter(contentScope, api)
    val messenger = MessageStore()
    column(MarginTop(1)) {
        filigree {
            heading4("Report a Bug")
        }

        column {
            centeredText("Something broken? Tell us what happened and what ye expected.")
            markdownEditor(model.descriptionState, "bug", MinHeight(32))
            formSubmit("Send", {
                model.report(messenger, portal.stateNow.route.screen as? Screen, portal.sitePath)
            }, messenger)
        }
    }
}
