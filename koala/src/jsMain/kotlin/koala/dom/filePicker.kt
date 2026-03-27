package koala.dom

import koala.css.*
import koala.html.FilePickerKey
import kotlinx.html.InputType
import kotlinx.html.hidden
import kotlinx.html.js.*
import org.w3c.dom.DragEvent
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.url.URL
import org.w3c.files.File
import org.w3c.files.get

fun DOMContext.filePicker(
    mimeType: MimeType = MimeType.All,
    modifiers: ModifierSet? = null,
    onMessage: ((UIMessage) -> Unit)? = null,
    onPickFile: (String) -> Unit
) {
    var input: HTMLInputElement
    var preview: HTMLImageElement
    var dropZone: HTMLDivElement
    val element = div {
        addModifiers(FilePickerKey.Class, modifiers)

        input = input {
            type = InputType.file
            accept = mimeType.expression
            hidden = true
        }

        dropZone = div {
            addModifiers(FilePickerKey.DropZone)
            +"Click here"
            br { }
            +"— or —"
            br { }
            +"Drop $mimeType"
        }

        preview = img {
            addModifiers(BorderRadius1)
        }
    }

    fun handleFile(file: File?) {
        val file = file ?: return
        if (!file.type.startsWith(mimeType.label)) {
            console.log("nay: $mimeType")
            onMessage?.invoke(UIMessage("Chosen file needs to be an image", UIMessageType.Error))
            return
        }

        val url = URL.createObjectURL(file)
        dropZone.style.display = "none"
        preview.src = url
        console.log(url)
        onPickFile(url)
    }

    element.addEventListener("drop", { event ->
        val event = event as DragEvent
        event.preventDefault()
        element.unmodify(FilePickerKey.DragOver)
        handleFile(event.dataTransfer?.files?.get(0))
    })

    element.onClick {
        input.click()
    }

    element.addEventListener("dragover", { event ->
        event.preventDefault()
        element.modify(FilePickerKey.DragOver)
    })

    element.addEventListener("dragleave", {
        element.unmodify(FilePickerKey.DragOver)
    })

    input.addEventListener("change", { _ ->
        handleFile(input.files?.get(0))
    })
}

enum class MimeType(val label: String) {
    All("*"),
    Image("image");

    val expression get() = "$label/*"
}