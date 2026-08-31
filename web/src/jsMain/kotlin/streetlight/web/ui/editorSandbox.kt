package streetlight.web.ui

import kampfire.api.Markdown
import koala.SvgFile
import koala.css.JustifyContentEnd
import koala.css.Magic
import koala.css.Scale
import koala.css.modify
import koala.dom.ViewScope
import koala.dom.button
import koala.dom.column
import koala.dom.filigree
import koala.dom.flowBlock
import koala.dom.flowModifier
import koala.dom.markdown
import koala.dom.row
import koala.dom.styledMarkdownEditor
import koala.html.heading1
import koala.model.EditorStyle
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
        row(modify(JustifyContentEnd)) {
            button(SvgFile.Magic, {
                textState.set { Markdown("$value!") }
            })
            button(SvgFile.Eye, isSwyg::toggle)
            button(SvgFile.Edit, isEditingState::toggle)
        }
        flowBlock(isEditingState, modify(Magic, Scale)) { isEditing ->
            when (isEditing) {
                true -> styledMarkdownEditor(textState, "sandbox").flowModifier(isSwyg, EditorStyle.SWYG, contentScope)
                else -> markdown(textState.now)
            }
        }
    }

    appFooter("")
}