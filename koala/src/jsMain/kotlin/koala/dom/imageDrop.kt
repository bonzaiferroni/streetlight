package koala.dom

import koala.Image
import koala.SvgFile
import koala.modifier.*
import kampfire.model.MutableTap
import koala.toImage

fun ViewScope.imageDrop(
    field: MutableTap<Image?>,
    mod: Modifier? = null,
    block: ViewScope.(Image) -> Unit
) = flowBlock(field, modify(mod, Magic, Scale)) { url ->
    if (url != null) {
        box(modify(Size100P, OverflowHidden)) {
            block(url)
            button(
                mod = modify(SystemBg, Outline, BorderRadius50P, PlaceSelfStart, Aspect1, Padding(1), Margin1, OpacityHigh),
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
    mod: Modifier? = null,
) = imageDrop(field, mod) {
    image(it.url, modify(Size100P))
}