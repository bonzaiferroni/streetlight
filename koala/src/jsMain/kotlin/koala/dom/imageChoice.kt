package koala.dom

import koala.css.*
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLButtonElement

fun RenderContext.imageChoice(
    modifiers: ModifierSet? = null,
    onValueChanged: ((String?) -> Unit)? = null,
    onUpload: suspend (String) -> String?
) {
    var localUrl: String? = null
    var uploadButton: HTMLButtonElement? = null

    val element = box(modify(SetImageClass.parent, modifiers)) {
        box(modify(SetImageClass.placeholder))
    }

    val dialog = dialogBox("Choose yer image") { close ->
        textBlock("[recent images]")
        filePicker("image") {
            localUrl = it
            uploadButton?.disabled = false
        }
        row(modify(FlexItems1)) {
            button("cancel", modify(Secondary), onClick = {
                close()
            })
            uploadButton = button("upload", modify(Accent), onClick = {
                val localUrl = localUrl ?: return@button
                renderScope.launch {
                    console.log("uploading: $localUrl")
                    val msg = onUpload(localUrl)
                    console.log(msg)
                    close()
                }
            }) {
                disabled = true
            }
        }
    }

    element.onClick {
        dialog.open()
    }
}

object SetImageClass {
    val parent = Css("set-image")
    val placeholder = Css("set-image-placeholder")
}