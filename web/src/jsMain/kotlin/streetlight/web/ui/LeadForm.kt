package streetlight.web.ui

import koala.dom.*
import streetlight.web.model.LeadEditor

/** The form that sends a url as a lead for the crawler to read. */
fun ViewScope.leadForm(editor: LeadEditor) = formCard("Lead") {
    formSection("lead") {
        column {
            textField(editor.urlState, "url")
            formSubmit("Submit", editor::submit, editor.message)
            centeredText("or send us a page to read")
        }
    }
}
