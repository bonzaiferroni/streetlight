package streetlight.web.ui

import kampfire.api.Markdown
import koala.SvgFile
import koala.modifier.*
import koala.dom.ViewScope
import koala.dom.component.button
import koala.dom.layout.column
import koala.dom.component.filigree
import koala.dom.layout.flowBlock
import koala.dom.flowModifier
import koala.dom.markdown.markdown
import koala.dom.layout.row
import koala.dom.markdown.markdownEditor
import koala.component.heading1
import koala.markdown.EditorStyle
import kampfire.model.storeOf
import kampfire.model.toggle
import streetlight.web.utils.localStoreOf

fun ViewScope.editorSandbox() {
    filigree {
        heading1("Yer Sandbox")
    }

    val textState = localStoreOf("sandbox-md", Markdown(""))
    val isEditingState = storeOf(true)
    val isSwyg = storeOf(false)
    column {
        row(JustifyContentEnd) {
            button(SvgFile.Magic.filled, {
                textState.set { Markdown("$value!") }
            })
            button(SvgFile.Eye.large, isSwyg::toggle)
            button(SvgFile.Edit.filled, isEditingState::toggle)
        }
        flowBlock(isEditingState, modify(Magic, Scale)) { isEditing ->
            when (isEditing) {
                true -> markdownEditor(textState, "sandbox").flowModifier(isSwyg, EditorStyle.SWYG, contentScope)
                else -> markdown(textState.now)
            }
        }
    }

    appFooter(sourcePathJsUi("editorSandbox.kt"))
}