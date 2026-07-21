package koala.dom

import koala.Image
import koala.css.AlignSelfStart
import koala.css.Blur
import koala.css.BorderRadius1
import koala.css.BorderRadius2
import koala.css.JustifySelfStart
import koala.css.Magic
import koala.css.Margin1
import koala.css.MinWidthAuto
import koala.css.ModifierSet
import koala.css.OpacityHigh
import koala.css.OverflowHidden
import koala.css.Secondary
import koala.css.Size100P
import koala.css.SlideDown
import koala.css.ZIndex1
import koala.css.modify
import koala.html.fillImage
import koala.model.MutableField
import koala.toImage

fun ViewScope.imageDrop(
    field: MutableField<Image?>,
    mod: ModifierSet? = null,
    block: ViewScope.(Image) -> Unit = {
        box(modify(Size100P)) {
            fillImage(it.url)
        }
    }
) = flowBlock(field, modify(mod, Magic, Blur, SlideDown)) { url ->
    if (url != null) {
        box(modify(Size100P, OverflowHidden, BorderRadius1)) {
            block(url)
            button(
                text = "✕",
                mod = modify(Secondary, MinWidthAuto, JustifySelfStart, AlignSelfStart, Margin1, OpacityHigh, ZIndex1),
                onClick = {
                    field.set(null)
                })
        }
    } else {
        filePicker(MimeType.Image, modify(Size100P, BorderRadius2)) {
            field.set(it.toImage())
        }
    }
}