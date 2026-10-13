package streetlight.web.view

import kabinet.utils.toRelativeTimeFormat
import koala.modifier.*
import koala.dom.ViewScope
import koala.dom.component.box
import koala.dom.component.button
import koala.dom.component.dialog
import koala.dom.component.dialogContent
import koala.dom.component.dropMenu
import koala.dom.component.grid
import koala.dom.component.row
import koala.dom.component.textBlock
import koala.component.spacer
import kampfire.model.setTrue
import kampfire.model.storeOf
import kotlinx.css.LinearDimension
import kotlinx.css.fr
import streetlight.model.data.EditLog
import streetlight.model.data.EditType
import streetlight.model.data.RecordEdit
import streetlight.model.data.verb
import streetlight.model.utils.TextDeltaDisplay
import streetlight.web.layout.TextDeltaStyle
import streetlight.web.dom.layout.deltaGrid

/** The edits of a record, each with a dialog that shows [content] for the edit and the one before it. */
inline fun <reified T: RecordEdit> ViewScope.viewEditHistory(
    editLogs: List<EditLog>,
    crossinline content: ViewScope.(T, T?) -> Unit
) {
    val display = storeOf(TextDeltaDisplay.Combined)

    grid(columnsOf(1.fr, LinearDimension.auto)) {
        var previousEdit: T? = null
        editLogs.forEachIndexed { index, log ->
            val timeDescription = log.createdAt.toRelativeTimeFormat()
            val edit = log.recordEdit as? T ?: return@forEachIndexed
            val compareEdit = previousEdit
            textBlock("${log.username} ${log.editType.verb} the location $timeDescription")

            row(JustifyContentEnd) {
                println("ey 0")
                if (log.editType == EditType.Update && index == editLogs.size - 1) {
                    button {
                        +"revert"
                    }
                }

                val dialogState = storeOf(false)
                dialog(dialogState) {
                    val content = dialogContent(timeDescription) {
                        row {
                            spacer(Flex1)
                            dropMenu(display)
                        }
                        box {
                            deltaGrid(mod = TextDeltaStyle.Highlighter) {
                                content(edit, compareEdit)
                            }
                        }
                    }

                    launchEffect {
                        display.flow.collect {
                            content.setAttribute(TextDeltaStyle.Display.to(it))
                        }
                    }
                }
                button(dialogState::setTrue) {
                    +"view"
                }
            }
            previousEdit = edit
        }
    }
}