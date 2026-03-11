package koala.dom

import koala.css.AlignSelfStart
import koala.css.Blur
import koala.css.BorderRadius1
import koala.css.JustifySelfEnd
import koala.css.Margin2
import koala.css.ModifierSet
import koala.css.OverflowHidden
import koala.css.Padding2
import koala.css.Secondary
import koala.css.Size100
import koala.css.SlideDown
import koala.css.Width100
import koala.css.modify
import kotlinx.coroutines.flow.Flow
import kotlinx.html.DIV

fun RenderContext.imageDrop(
    urlFlow: Flow<String?>,
    onFileUrl: (String?) -> Unit,
    modifiers: ModifierSet? = null,
    block: RenderContext.(String) -> Unit = { image(it, modify(Width100)) }
) {
    flowBlock(urlFlow, modify(modifiers, Blur, SlideDown), magic = true) { url ->
        if (url != null) {
            box(modify(Size100, OverflowHidden, BorderRadius1)) {
                block(url)
                button("✕", modify(Secondary, JustifySelfEnd, AlignSelfStart, Margin2), onClick = {
                    onFileUrl(null)
                })
            }
        } else {
            filePicker(MimeType.Image, modify(Size100)) {
                console.log(it)
                onFileUrl(it)
            }
        }
    }
}