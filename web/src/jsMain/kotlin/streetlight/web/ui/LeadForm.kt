package streetlight.web.ui

import koala.dom.*
import streetlight.model.data.LeadType
import streetlight.web.model.LeadEditor

/** The form that sends a url as a lead for the crawler to read, worded for the editor's kind of lead. */
fun ViewScope.leadForm(editor: LeadEditor) = formCard("Lead") {
    val (placeholder, help) = when (editor.leadType) {
        LeadType.Event -> "event page url" to "or send us the event's page"
        else -> "homepage url" to "or send us their website"
    }
    formSection("lead") {
        column {
            textField(editor.urlState, placeholder)
            formSubmit("Submit", editor::submit, editor.message)
            centeredText(help)
        }
    }
}
