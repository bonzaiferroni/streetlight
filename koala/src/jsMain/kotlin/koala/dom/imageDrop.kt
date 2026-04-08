package koala.dom

import kampfire.model.Url
import koala.css.AlignSelfStart
import koala.css.Blur
import koala.css.BorderRadius1
import koala.css.JustifySelfEnd
import koala.css.Magic
import koala.css.Margin1
import koala.css.MinWidthAuto
import koala.css.ModifierSet
import koala.css.OpacityMost
import koala.css.OverflowHidden
import koala.css.Secondary
import koala.css.Size100P
import koala.css.SlideDown
import koala.css.ZIndex1
import koala.css.modify
import koala.html.fillImage
import kotlinx.coroutines.flow.Flow

fun RenderContext.imageDrop(
    urlFlow: Flow<Url?>,
    onFileUrl: (Url?) -> Unit,
    modifiers: ModifierSet? = null,
    block: RenderContext.(Url) -> Unit = {
        box(modify(Size100P)) {
            fillImage(it)
        }
    }
) {
    flowBlock(urlFlow, modify(modifiers, Magic, Blur, SlideDown)) { url ->
        if (url != null) {
            box(modify(Size100P, OverflowHidden, BorderRadius1)) {
                block(url)
                button(
                    text = "✕",
                    modifiers = modify(Secondary, MinWidthAuto, JustifySelfEnd, AlignSelfStart, Margin1, OpacityMost, ZIndex1),
                    onClick = {
                        onFileUrl(null)
                    })
            }
        } else {
            filePicker(MimeType.Image, modify(Size100P)) {
                console.log(it)
                onFileUrl(it)
            }
        }
    }
}