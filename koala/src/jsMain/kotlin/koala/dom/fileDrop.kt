package koala.dom

import koala.css.Blur
import koala.css.SlideDown
import koala.css.SlideUp
import koala.css.modify
import koala.model.storeOf
import kotlinx.coroutines.flow.Flow

fun RenderContext.fileDrop(urlFlow: Flow<String?>, onFileUrl: (String) -> Unit) {
    flowBlock(urlFlow, modify(Blur, SlideDown), magic = true) { url ->
        if (url != null) {
            textBlock("file: $url")
        } else {
            filePicker(null) {
                console.log(it)
                onFileUrl(it)
            }
        }
    }
}