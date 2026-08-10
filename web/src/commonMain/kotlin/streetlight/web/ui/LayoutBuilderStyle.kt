package streetlight.web.ui

import koala.css.Class

object LayoutBuilderStyle {
    val blockOverlay = Class("builder-block-overlay")
}

// language="CSS"
val LayoutBuilderCss get() = with(LayoutBuilderStyle) { """
$blockOverlay {
    > * {
        position: relative;
        &::after {
            content: "";
            position: absolute;
            inset: 0;
            background-color: var(--selection-overlay);
        }
    }
}
"""}