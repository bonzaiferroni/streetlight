package streetlight.web.ui

import kabinet.utils.toRelativeTimeFormat
import koala.css.Flex1
import koala.css.JustifyContentEnd
import koala.css.columnsOf
import koala.css.modify
import koala.dom.ViewScope
import koala.dom.box
import koala.dom.button
import koala.dom.dialog
import koala.dom.dialogCard
import koala.dom.dialogContent
import koala.dom.dropMenu
import koala.dom.grid
import koala.dom.row
import koala.dom.setAttribute
import koala.dom.textBlock
import koala.html.spacer
import koala.model.storeOf
import kotlinx.css.LinearDimension
import kotlinx.css.fr
import streetlight.model.data.EditLog
import streetlight.model.data.EditType
import streetlight.model.data.RecordEdit
import streetlight.model.data.verb
import streetlight.model.utils.TextDeltaDisplay

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

            row(modify(JustifyContentEnd)) {
                if (log.editType == EditType.Update && index == editLogs.size - 1) {
                    button {
                        +"revert"
                    }
                }

                val dialogState = storeOf(false)
                dialog(dialogState) {
                    val content = dialogContent(timeDescription) {
                        row {
                            spacer(modify(Flex1))
                            dropMenu(display)
                        }
                        box {
                            deltaGrid(mod = modify(TextDeltaStyle.Highlighter)) {
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
            }
            previousEdit = edit
        }
    }
}