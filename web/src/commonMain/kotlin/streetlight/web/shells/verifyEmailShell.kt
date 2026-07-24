package streetlight.web.shells

import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import koala.html.column
import koala.html.textBlock
import kotlinx.html.FlowContent
import streetlight.web.ui.BodyStyle

fun FlowContent.verifyEmailShell(outcome: Outcome<String>) {
    column(BodyStyle.Column) {
        val message = when (outcome) {
            is Problem -> "There was a problem: ${outcome.message}"
            is Ok -> outcome.data
        }
        textBlock(message)
    }
}