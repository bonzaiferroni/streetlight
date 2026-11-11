@file:Suppress("CssInvalidHtmlTagReference")

package streetlight.web.view

import koala.modifier.Class
import koala.modifier.transition

val PrestoSelf = Class("presto-self")

object Presto {
    val Height = transition(PrestoKey.Height)
    val CardFocus = transition(PrestoKey.CardFocus)
}

object PrestoKey {
    const val Height = "presto-height"
    const val CardFocus = "presto-card-focus"
}

//language="CSS"
val PrestoCss get() = with(PrestoKey) { """

/* an element that survives a change, paired with itself */
$PrestoSelf { view-transition-name: match-element; }

/* the snapshots fill the animating box, clipped by it and anchored to its top */
::view-transition-old($Height),
::view-transition-new($Height) {
    height: 100%;
    object-fit: none;
    object-position: top;
    overflow: clip;
}

/* the card's box animates with true corners, clipping snapshots kept at their natural size */
::view-transition-group($CardFocus) {
    border-radius: calc(var(--curve-unit) * 2);
    overflow: clip;
}

::view-transition-old($CardFocus),
::view-transition-new($CardFocus) {
    width: 100%;
    height: 100%;
    object-fit: none;
    object-position: top left;
}

"""}
