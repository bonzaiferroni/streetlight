package streetlight.web.ui

import kampfire.api.Markdown
import koala.SvgFile
import koala.css.JustifyContentEnd
import koala.css.Magic
import koala.css.Scale
import koala.css.modify
import koala.dom.*
import koala.html.heading1
import koala.model.storeOf
import koala.model.toggle
import streetlight.web.utils.localStoreOf

fun RouteScope.viewSandbox() {
    // val model = Sandbox(contentScope, api, toaster)

    console.log("welcome to sandbox")

    filigree {
        heading1("Yer Sandbox")
    }

    val textState = localStoreOf("sandbox-md", Markdown(""))
    val isEditingState = storeOf(true)
    column {
        row(modify(JustifyContentEnd)) {
            button(SvgFile.Magic, {
                textState.set { Markdown("$value!") }
            })
            button(SvgFile.Edit, isEditingState::toggle)
        }
        flowBlock(isEditingState, modify(Magic, Scale)) { isEditing ->
            when (isEditing) {
                true -> styledMarkdownEditor(textState)
                else -> markdown(textState.now)
            }
        }
    }
}

class SandboxException : Exception("Arrr sandbox exception")

