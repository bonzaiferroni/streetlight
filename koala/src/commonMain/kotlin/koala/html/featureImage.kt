package koala.html

import koala.Image
import koala.SiteImage
import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.img

// the content image is contained over a blurred backdrop of the same image
fun FlowContent.containImage(
    image: Image? = null,
    mod: Modifier? = null,
    placeholder: Image = SiteImage.placeholder,
    alt: String? = null,
    lazy: Boolean = true,
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
            configureImage(null, image, ContainImage.ContentClass, alt, lazy) { }
        }
    }
}

object ContainImage {
    val Class = Class("contain-image")
    val BackdropClass = Class.withBemElement("backdrop")
    val ContentClass = Class.withBemElement("content")
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
""" }