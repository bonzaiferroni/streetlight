package koala.html

import kampfire.model.Url
import koala.css.Class
import koala.css.ModifierSet
import koala.css.addModifiers
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.img

fun FlowContent.headerImage(
    text: String,
    src: Url?,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    box {
        addModifiers(HeaderImageKey.Class, modifiers)
        block()

        src?.let {
            img {
                this.src = src.value
            }
        }

        heading1(text)
    }
}

object HeaderImageKey {
    val Class = Class("header-image")
}

// language="CSS"
val HeaderImageCss get() = """
.header-image {
    display: grid;
    width: 100%;
    max-height: 24rem;
    border-radius: 1rem;
    overflow: hidden;
}

.header-image > * {
    grid-area: 1 / 1 / 2 / 2;
}

/* Make the image fill the box, crop from center vertically if too tall */
.header-image > img {
    width: 100%;
    height: 100%;
    max-height: 24rem;
    object-fit: cover;
    object-position: center center;
    display: block;
}

/* Gradient overlay: transparent until halfway, then to 50% black at bottom */
.header-image::after {
    content: "";
    grid-area: 1 / 1 / 2 / 2;
    pointer-events: none;
    background: linear-gradient(
            to bottom,
            rgba(0, 0, 0, 0.1) 0%,
            rgba(0, 0, 0, 0.1) 50%,
            rgba(0, 0, 0, 0.8) 100%
    );
}

/* Title centered horizontally, near the bottom, with a drop shadow */
.header-image > h1 {
    align-self: end;
    justify-self: end;
    margin: 0;
    padding: 1.25rem 1.5rem;
    text-align: center;
    z-index: 1;
    color: white;
    text-shadow: 0 0.25rem 0.75rem rgba(0, 0, 0, 0.8);
}
"""
