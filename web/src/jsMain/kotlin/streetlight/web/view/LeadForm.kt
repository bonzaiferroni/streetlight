package streetlight.web.view

import koala.dom.*
import streetlight.web.model.LeadEditor
import koala.dom.component.*
import streetlight.web.dom.layout.centeredText
import streetlight.web.dom.layout.formCard
import streetlight.web.dom.layout.formSection
import streetlight.web.dom.layout.formSubmit

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
