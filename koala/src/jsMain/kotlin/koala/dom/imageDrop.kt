package koala.dom

import koala.Image
import koala.SvgFile
import koala.css.*
import koala.html.fillImage
import koala.model.MutableTap
import koala.toImage

fun ViewScope.imageDrop(
    field: MutableTap<Image?>,
    mod: ModifierSet? = null,
    block: ViewScope.(Image) -> Unit
) = flowBlock(field, modify(mod, Magic, Scale)) { url ->
    if (url != null) {
        box(modify(Size100P, OverflowHidden, BorderRadius1)) {
            block(url)
            button(
                mod = modify(EditorBg, Outline, BorderRadius50P, PlaceSelfStart, Aspect1, Padding1, Margin1, OpacityHigh),
                onClick = {
                    field.set(null)
                }) {
                icon(SvgFile.X)
            }
        }
    } else {
        filePicker(MimeType.Image, modify(Size100P, BorderRadius2)) {
            field.set(it.toImage())
        }
    }
}

fun ViewScope.imageDrop(
    field: MutableTap<Image?>,
    mod: ModifierSet? = null,
) = imageDrop(field, mod) {
    box(modify(Size100P)) {
        fillImage(it.url, modify(Size100P))
    }
}