package koala.dom

import kampfire.model.Url
import koala.css.*
import koala.html.ImageChooserKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.dom.clear
import kotlinx.html.dom.append
import kotlinx.html.js.*
import kotlinx.html.style
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLImageElement

fun RenderContext.imageChooser(
    modifiers: ModifierSet? = null,
    onValueChanged: ((Url?) -> Unit)? = null,
    onUpload: suspend (Url) -> Url?,
    urlFlow: Flow<Url?>? = null,
    choicesFlow: Flow<List<Url>>? = null,
): HTMLDivElement {
    var localUrl: Url? = null
    var uploadButton: HTMLButtonElement? = null
    var image: HTMLImageElement? = null
    var placeholder: HTMLDivElement? = null
    var choicesRow: HTMLDivElement? = null
    var choiceUrl: Url? = null
    var choices: List<Url>? = null
    var isInitialized = false

    val element = box(modify(ImageChooserKey.Class, modifiers)) {
        placeholder = box(modify(ImageChooserKey.Placeholder))
        image = img {
            addModifiers(modify(BorderRadius1, MaxHeight64))
            style = "display: none;"
        }
    }

    val dialog = dialogBox("Choose yer image") { close ->
        choicesRow = row {
            addModifiers(FlexWrap)
            style = "display: none;"
        }
        filePicker(MimeType.Image) {
            localUrl = it
            uploadButton?.disabled = false
        }
        row(modify(FlexItems1)) {
            button("cancel", modify(Secondary), onClickEvent = {
                close()
            })
            uploadButton = button("upload", modify(Accent), onClickEvent = {
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

    fun initializeChoiceRow() {
        val choicesRow = choicesRow ?: return
        choicesRow.clear()
        choicesRow.append {
            choiceUrl?.let { choiceUrl ->
                val image = img(src = choiceUrl.value) {
                    addModifiers(Height16)
                }
                image.onClick {
                    dialog.close()
                }
            }
            choices?.forEach { url ->
                val image =img(src = url.value) {
                    addModifiers(Height16)
                }
                image.onClick {
                    onValueChanged?.invoke(url)
                    dialog.close()
                }
            }
        }
        if (choiceUrl == null && choices.isNullOrEmpty()) {
            choicesRow.style.display = "none"
        } else {
            choicesRow.style.display = "flex"
        }
    }

    element.onClick {
        if (!isInitialized) {
            isInitialized = true
            initializeChoiceRow()
        }
        dialog.open()
    }

    renderScope.launch {
        launch {
            val image = image ?: return@launch
            val placeholder = placeholder ?: return@launch
            urlFlow?.collect { url ->
                isInitialized = false
                choiceUrl = url
                if (url != null) {
                    image.src = url.value
                    image.style.display = "block"
                    placeholder.style.display = "none"
                } else {
                    image.style.display = "none"
                    placeholder.style.display = "block"
                }
            }
        }
        launch {
            choicesFlow?.collect {
                choices = it
                isInitialized = false
            }
        }
    }

    return element
}

