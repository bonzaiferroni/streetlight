package koala.dom

import koala.css.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.dom.clear
import kotlinx.html.dom.append
import kotlinx.html.js.*
import kotlinx.html.style
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLImageElement

fun RenderContext.imageChoice(
    modifiers: ModifierSet? = null,
    onValueChanged: ((String?) -> Unit)? = null,
    onUpload: suspend (String) -> String?,
    urlFlow: Flow<String?>? = null,
    choicesFlow: Flow<List<String>>? = null,
): HTMLDivElement {
    var localUrl: String? = null
    var uploadButton: HTMLButtonElement? = null
    var image: HTMLImageElement? = null
    var placeholder: HTMLDivElement? = null
    var choicesRow: HTMLDivElement? = null

    val element = box(modify(SetImageClass.parent, modifiers)) {
        placeholder = box(modify(SetImageClass.placeholder))
        image = img {
            applyModifiers(BorderRadius1)
            style = "display: none;"
        }
    }

    val dialog = dialogBox("Choose yer image") { close ->
        choicesRow = row {
            applyModifiers(WrapFlex)
            style = "display: none;"
        }
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
                    val url = onUpload(localUrl)
                    onValueChanged?.invoke(url)
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

    renderScope.launch {
        launch {
            val image = image ?: return@launch
            val placeholder = placeholder ?: return@launch
            urlFlow?.collect { url ->
                if (url != null) {
                    image.src = url
                    image.style.display = "block"
                    placeholder.style.display = "none"
                } else {
                    image.style.display = "none"
                    placeholder.style.display = "block"
                }
            }
        }
        launch {
            val choicesRow = choicesRow ?: return@launch
            choicesFlow?.collect { choices ->
                if (choices.isEmpty()) {
                    choicesRow.style.display = "none"
                } else {
                    choicesRow.clear()
                    choicesRow.append {
                        choices.forEach { url ->
                            val image =img(src = url) {
                                applyModifiers(Height16)
                            }
                            image.onClick {
                                onValueChanged?.invoke(url)
                                dialog.close()
                            }
                        }
                    }
                    choicesRow.style.display = "flex"
                }
            }
        }
    }

    return element
}

object SetImageClass {
    val parent = Css("set-image")
    val placeholder = Css("set-image-placeholder")
}