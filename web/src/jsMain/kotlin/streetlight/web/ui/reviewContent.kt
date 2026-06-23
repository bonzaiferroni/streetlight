package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.heading3
import koala.html.textBlock
import streetlight.model.data.EditReview
import streetlight.model.data.LocationEdit
import streetlight.model.data.QuorumReview

fun AppScope.reviewContent(quorumReview: QuorumReview) {
    when (quorumReview) {
        is EditReview -> editReviewContent(quorumReview)
    }
}

fun AppScope.editReviewContent(editReview: EditReview) {
    val quorum = editReview.quorum
    val edit = editReview.editLog.recordEdit as? LocationEdit ?: error("edit not found")
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