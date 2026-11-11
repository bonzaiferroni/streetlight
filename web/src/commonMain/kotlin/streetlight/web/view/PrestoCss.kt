@file:Suppress("CssInvalidHtmlTagReference")

package streetlight.web.view

import koala.modifier.transition

object Presto {
    val Height = transition(PrestoKey.Height)
}

object PrestoKey {
    const val Height = "presto-height"
    const val AppFooter = "app-footer"
}

//language="CSS"
val PrestoCss get() = with(PrestoKey) { """

/* the snapshots fill the animating box, clipped by it and anchored to its top */
::view-transition-old($Height),
::view-transition-new($Height) {
    height: 100%;
    object-fit: none;
    object-position: top;
    overflow: clip;
}

"""}
