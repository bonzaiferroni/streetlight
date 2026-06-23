package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.heading3
import koala.html.textBlock
import streetlight.model.data.QuorumReviewContent
import streetlight.model.data.LocationEdit

//fun AppScope.reviewContent(task: QuorumReviewTask) {
//    when (quorumTask) {
//        is QuorumReviewTask -> editReviewContent(quorumTask)
//    }
//}

fun AppScope.editReviewContent(editReviewTask: QuorumReviewContent) {
    val quorum = editReviewTask.quorum
    val edit = editReviewTask.editLog.recordEdit as? LocationEdit ?: error("edit not found")
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