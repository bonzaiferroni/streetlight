package koala.dom

import kampfire.model.Url
import koala.css.Blur
import koala.css.Magic
import koala.css.SlideDown
import koala.css.modify
import kotlinx.coroutines.flow.Flow

fun RenderContext.fileDrop(urlFlow: Flow<String?>, onFileUrl: (Url) -> Unit) {
    flowBlock(urlFlow, modify(Magic, Blur, SlideDown)) { url ->
        if (url != null) {
            textBlock("file: $url")
        } else {
            filePicker() {
                console.log(it)
                onFileUrl(it)
            }
        }
    }
}