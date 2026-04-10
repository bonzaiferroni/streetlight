package koala.html

import kampfire.model.Url
import koala.SiteImage
import koala.css.Class
import koala.css.ModifierSet
import koala.css.addModifiers
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.IMG
import kotlinx.html.div
import kotlinx.html.img

fun FlowContent.featureImage(
    src: Url? = null,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    val src = src?.value ?: SiteImage.placeholderLg.url.value
    div {
        addModifiers(Class, modifiers)
        block()

        img {
            addModifiers(BackdropClass)
            this.src = src
        }

        img {
            addModifiers(ContentClass)
            this.src = src
        }
    }
}

private val Class = Class("feature-image")
private val BackdropClass = Class("feature-image__backdrop")
private val ContentClass = Class("feature-image__content")

// language="CSS"
val FeatureImageCss get() = """
$Class {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: clip;
    min-width: 0;
}

$BackdropClass {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
    object-fit: fill;
    transform: scale(1.2);
    filter: blur(24px);
}

$ContentClass {
    position: relative;
    min-width: 0;
}
"""