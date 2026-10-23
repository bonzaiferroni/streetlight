package streetlight.web.model

import kampfire.api.Markdown
import kampfire.api.toMarkdown
import kampfire.model.Tap
import kampfire.model.mutableTapOf
import kampfire.model.reactIn
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.InterviewQuestion

/**
 * Edits the answers of an [interview], following the questions [questionsTap] asks in [scope].
 *
 * Each change to the questions keeps every entry that has an answer or is still asked, in its order, and appends
 * each question newly asked.
 */
class InterviewEditor(
    interview: List<InterviewQuestion>,
    questionsTap: Tap<List<String>>,
    scope: CoroutineScope,
) {
    private val state = storeOf(InterviewEditorState(rebuildEntries(interview, questionsTap.now)))

    val questionsState = state.tapOf { state -> state.entries.map { it.question } }

    /** The answered entries, as they are saved. */
    val interviewNow get() = state.now.entries.filter { it.answer.isNotBlank() }

    init {
        questionsTap.reactIn(scope) { questions ->
            state.set { copy(entries = rebuildEntries(entries, questions)) }
        }
    }

    /** The answer to [question]. */
    fun answerState(question: String) = state.mutableTapOf(
        { state -> state.entries.firstOrNull { it.question == question }?.answer ?: "".toMarkdown() }
    ) { answer ->
        copy(entries = entries.map { if (it.question == question) it.copy(answer = answer) else it })
    }
}

data class InterviewEditorState(
    val entries: List<InterviewQuestion>,
)

private fun rebuildEntries(entries: List<InterviewQuestion>, questions: List<String>): List<InterviewQuestion> {
    val kept = entries.filter { it.answer.isNotBlank() || it.question in questions }
    val added = questions.distinct()
        .filter { question -> kept.none { it.question == question } }
        .map { InterviewQuestion(it, "".toMarkdown()) }
    return kept + added
}
