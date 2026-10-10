package koala.html

import koala.Image
import koala.SiteImage
import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.img

// the content image is contained over a blurred backdrop of the same image
/**
 * Shows [image] whole, contained over a blurred and scaled backdrop of the same image, falling back to
 * [placeholder].
 *
 * The fit is set on the root and inherited by the image, so a caller changes it with [mod]. When [isNative], the
 * image shows its largest variant at its own size, as wide as the root at most, and the backdrop fills the rest.
 */
fun FlowContent.containImage(
    image: Image? = null,
    mod: Modifier? = null,
    placeholder: Image = SiteImage.placeholder,
    alt: String? = null,
    lazy: Boolean = true,
    isNative: Boolean = false,
    block: DIV.() -> Unit = {}
) {
    val image = image ?: placeholder
    div {
        addModifiers(ContainImage.Class, mod)
        block()

        img {
            configureImage(null, image, ContainImage.BackdropClass, null, lazy) { }
        }

        img {
            when (isNative) {
                true -> configureImage(image.largest ?: image.url, null, ContainImage.NativeClass, alt, lazy) { }
                false -> configureImage(null, image, ContainImage.ContentClass, alt, lazy) { }
            }
        }
    }
}

object ContainImage {
    val Class = Class("contain-image")
    val BackdropClass = Class.withBemElement("backdrop")
    val ContentClass = Class.withBemElement("content")
    val NativeClass = Class.withBemElement("native")
}

// language="CSS"
val ContainImageCss get() = with(ContainImage) { """
$Class {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: clip;
    min-width: 0;
    object-fit: contain;
}

$BackdropClass {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
    object-fit: fill;
    transform: scale(1.2);
    filter: blur(24px) brightness(0.8);
}

$ContentClass {
    width: 100%;
    height: 100%;
    position: relative;
    min-width: 0;
    min-height: 0;
    object-fit: inherit;
}

/* the image at its own size, centered by the root */
$NativeClass {
    position: relative;
    max-width: 100%;
}
""" }
