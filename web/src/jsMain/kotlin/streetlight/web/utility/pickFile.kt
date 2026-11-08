package streetlight.web.utility

import web.html.InputType
import web.html.file
import web.dom.document
import web.events.CHANGE
import web.events.Event
import web.events.addEventListener
import web.file.File
import web.html.HTMLInputElement

/** Asks the viewer for a file matching [accept], an input `accept` list, and hands the one picked to [onPickFile]. */
fun pickFile(accept: String, onPickFile: (File) -> Unit) {
    val input = document.createElement("input") as HTMLInputElement
    input.type = InputType.file
    input.accept = accept
    input.addEventListener(Event.CHANGE, { _ ->
        input.files?.get(0)?.let(onPickFile)
    })
    input.click()
}
