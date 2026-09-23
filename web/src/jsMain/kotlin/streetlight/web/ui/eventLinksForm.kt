package streetlight.web.ui

import koala.modifier.*
import koala.dom.*
import kotlinx.css.pct
import streetlight.web.model.EventEditor

fun ViewScope.eventLinksForm(model: EventEditor) {
    formCard("Event Links") {
        formRow {
            formSection("Website") {
                textField(model.urlState, "Website", Width(100.pct))
                centeredText("Is there more information about this event on the web?")
            }
            formSection("Original Source") {
                textField(model.originalSourceLabelState, "Source name")
                centeredText("Want to give a shout out to the original place where you found the event?")
                textField(model.originalSourceUrlState, "Source url")
            }
        }

        linksFormSection(model.linksState)
    }
}