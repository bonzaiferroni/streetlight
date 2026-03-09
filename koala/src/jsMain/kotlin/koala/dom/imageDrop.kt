package koala.dom

import koala.css.AlignSelfStart
import koala.css.Blur
import koala.css.JustifySelfEnd
import koala.css.Margin2
import koala.css.Padding2
import koala.css.SlideDown
import koala.css.modify
import kotlinx.coroutines.flow.Flow
import kotlinx.html.DIV

fun RenderContext.imageDrop(
    urlFlow: Flow<String?>,
    onFileUrl: (String?) -> Unit,
    block: RenderContext.(String) -> Unit = { image(it) }
) {
    flowBlock(urlFlow, modify(Blur, SlideDown), magic = true) { url ->
        if (url != null) {
            box {
                block(url)
                button("clear", modify(JustifySelfEnd, AlignSelfStart, Margin2), onClick = {
                    onFileUrl(null)
                })
            }
        } else {
            filePicker(MimeType.Image) {
                console.log(it)
                onFileUrl(it)
            }
        }
    }
}