package koala.html

import koala.css.Class

object ImageChooserKey {
    val Class = Class("set-image")
    val Placeholder = Class("set-image-placeholder")
}

// language="CSS"
val ImageChooserCss get() = """
${ImageChooserKey.Class} {
    border-radius: var(--unit-spacing);
    position: relative;
}

${ImageChooserKey.Class} > img {
    margin: auto;
}

${ImageChooserKey.Placeholder} {
    background-color: var(--card-bg);
    height: 100%;
    width: 100%;
    position: absolute;
}

${ImageChooserKey.Placeholder}::before {
    content: "";
    position: absolute;
    inset: 0;

    background: var(--gray-fg);     /* SVG color */
    mask: url("/www/svg/image-placeholder.svg") no-repeat center / 2rem;
    -webkit-mask: url("/www/svg/image-placeholder.svg") no-repeat center / 2rem;

    pointer-events: none;
}
"""