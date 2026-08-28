package koala.dom

import kampfire.model.UIMessage
import kampfire.model.UIMessageType
import kampfire.model.Url
import kampfire.model.toUrl
import koala.css.*
import koala.html.FilePickerKey
import kotlinx.html.InputType
import kotlinx.html.hidden
import kotlinx.html.js.*
import web.dnd.DRAG
import web.dnd.DRAG_LEAVE
import web.dnd.DRAG_OVER
import web.dnd.DragEvent
import web.events.CHANGE
import web.events.Event
import web.events.addEventListener
import web.file.File
import web.html.HTMLDivElement
import web.html.HTMLImageElement
import web.html.HTMLInputElement
import web.input.InputEvent
import web.pointer.PointerEvent
import web.url.URL

fun AppendScope.filePicker(
    mimeType: MimeType = MimeType.All,
    mod: ModifierSet? = null,
    onMessage: ((UIMessage) -> Unit)? = null,
    onPickFile: (Url) -> Unit
) {
    var input: HTMLInputElement
    var preview: HTMLImageElement
    var dropZone: HTMLDivElement
    val element = div {
        addModifiers(FilePickerKey.Class, mod)

        input = input {
            type = InputType.file
            accept = mimeType.expression
            hidden = true
        }.asWeb()

        dropZone = div {
            addModifiers(FilePickerKey.DropZone)
            +"Click here"
            br { }
            +"— or —"
            br { }
            +"Drop $mimeType"
        }.asWeb()

        preview = img {
            addModifiers(BorderRadius1)
        }.asWeb()
    }.asWeb()

    fun handleFile(file: File?) {
        val file = file ?: return
        if (!file.type.startsWith(mimeType.label)) {
            console.log("nay: $mimeType")
            onMessage?.invoke(UIMessage("Chosen file needs to be an image", messageType = UIMessageType.Error))
            return
        }

        val url = URL.createObjectURL(file)
        dropZone.style.display = "none"
        preview.src = url
        onPickFile(url.toUrl())
    }

    element.addEventListener(DragEvent.DRAG, { event ->
        event.preventDefault()
        element.unmodify(FilePickerKey.DragOver)
        handleFile(event.dataTransfer?.files?.get(0))
    })

    element.onClick {
        input.click()
    }

    element.addEventListener(DragEvent.DRAG_OVER, { event ->
        event.preventDefault()
        element.modify(FilePickerKey.DragOver)
    })

    element.addEventListener(DragEvent.DRAG_LEAVE, {
        element.unmodify(FilePickerKey.DragOver)
    })

    input.addEventListener(Event.CHANGE, { _ ->
        handleFile(input.files?.get(0))
    })
}

enum class MimeType(val label: String) {
    All("*"),
    Image("image");

    val expression get() = "$label/*"
}