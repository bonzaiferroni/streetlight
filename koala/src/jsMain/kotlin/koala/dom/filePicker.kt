package koala.dom

import koala.css.*
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
    mimeType: String = "image",
    modifiers: ModifierSet? = null,
    onMessage: ((UIMessage) -> Unit)? = null,
    onPickFile: (String) -> Unit
) {
    var input: HTMLInputElement
    var preview: HTMLImageElement
    var dropZone: HTMLDivElement
    val element = div {
        applyModifiers(modifiers)

        input = input {
            type = InputType.file
            accept = "$mimeType/*"
            hidden = true
        }

        dropZone = div {
            applyModifiers(FilePickerClass.dropZone)
            +"Click here"
            br { }
            +"— or —"
            br { }
            +"Drop $mimeType"
        }

        preview = img {
            applyModifiers(BorderRadius1)
        }
    }

    fun handleFile(file: File?) {
        val file = file ?: return
        if (!file.type.startsWith("$mimeType/")) {
            onMessage?.invoke(UIMessage(UIMessageType.Error, "Chosen file needs to be an image"))
            return
        }

        val url = URL.createObjectURL(file)
        dropZone.style.display = "none"
        preview.src = url
        onPickFile(url)
    }

    element.addEventListener("drop", { event ->
        val event = event as DragEvent
        event.preventDefault()
        element.unmodify(FilePickerClass.dragover)
        handleFile(event.dataTransfer?.files?.get(0))
    })

    element.onClick {
        input.click()
    }

    element.addEventListener("dragover", { event ->
        event.preventDefault()
        element.modify(FilePickerClass.dragover)
    })

    element.addEventListener("dragleave", {
        element.unmodify(FilePickerClass.dragover)
    })

    input.addEventListener("change", { _ ->
        handleFile(input.files?.get(0))
    })
}

object FilePickerClass {
    val dropZone = Css("file-picker-drop-zone")
    val dragover = Css("dragover")
}

// <div id="dropZone" class="drop-zone">
//    Drop yer image here or click to choose
//    <input id="imgPick" type="file" accept="image/*" hidden>
//</div>
//<img id="preview" alt="">

// const zone = document.getElementById("dropZone");
//const input = document.getElementById("imgPick");
//const preview = document.getElementById("preview");
//
//const handleFile = (file) => {
//    if (!file || !file.type.startsWith("image/")) return;
//    preview.src = URL.createObjectURL(file);
//};
//
//zone.addEventListener("click", () => {
//    input.value = "";
//    input.click();
//});
//
//input.addEventListener("change", () => {
//    handleFile(input.files?.[0]);
//});
//
//zone.addEventListener("dragover", (e) => {
//    e.preventDefault();
//    zone.classList.add("dragover");
//});
//
//zone.addEventListener("dragleave", () => {
//    zone.classList.remove("dragover");
//});
//
//zone.addEventListener("drop", (e) => {
//    e.preventDefault();
//    zone.classList.remove("dragover");
//    handleFile(e.dataTransfer.files?.[0]);
//});