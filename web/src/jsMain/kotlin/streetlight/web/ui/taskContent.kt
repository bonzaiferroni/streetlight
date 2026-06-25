package streetlight.web.ui

import koala.css.modify
import koala.dom.AppScope
import koala.dom.column
import koala.dom.navigation
import koala.html.heading3
import koala.html.textBlock
import streetlight.model.data.EditTaskContent
import streetlight.model.data.LocationEdit
import streetlight.model.data.QuorumReviewContent
import streetlight.model.data.TaskContent

fun AppScope.taskContent(task: TaskContent) {
    when (task) {
        is EditTaskContent -> editTaskContent(task)
        is QuorumReviewContent -> quorumReviewContent(task)
    }
}

fun AppScope.editTaskContent(task: EditTaskContent) {
    val edit = task.editLog.recordEdit as? LocationEdit ?: return
    console.log(edit.locationId)
    val editor = app.getLocationEditor(edit, parentScope)
    column {
        textBlock("Check to make sure this information is complete.")
        locationEditFormBody(editor)
        formSubmit("done", editor::submit)
    }
}

fun AppScope.quorumReviewContent(task: QuorumReviewContent) {
    val quorum = task.quorum
    val edit = task.editLog.recordEdit as? LocationEdit ?: error("edit not found")
    column {
        heading3(quorum.question.label)
        deltaGrid(modify(TextDeltaStyle.Highlighter)) {
            deltaRow("name", edit.name)
            deltaRow("address", edit.address)
            deltaRow("description", edit.description)
            deltaRow("geolocation", edit.geoPoint)
            textBlock("Google maps")
            val pinUrl = "https://www.google.com/maps?q=${edit.geoPoint}"
            navigation(pinUrl) { +pinUrl }
        }
    }
}