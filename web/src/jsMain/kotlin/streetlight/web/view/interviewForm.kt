package streetlight.web.view

import koala.dom.ViewScope
import koala.dom.layout.flowBlock
import koala.dom.markdown.markdownEditor
import koala.modifier.*
import streetlight.web.dom.layout.formRow
import streetlight.web.dom.layout.formSection
import streetlight.web.model.InterviewEditor

/** A section for each question [model] asks, holding an editor of its answer. */
context(model: InterviewEditor)
fun ViewScope.interviewForm() = flowBlock(model.questionsState) { questions ->
    formRow {
        questions.forEach { question ->
            formSection(question) {
                markdownEditor(model.answerState(question), "answer", MinHeight(16))
            }
        }
    }
}
