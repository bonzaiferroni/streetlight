@file:Suppress("CssInvalidHtmlTagReference")

package streetlight.web.view

import koala.modifier.Class
import koala.modifier.Css

val PrestoSelf = Class("presto-self")

/** The view transition classes, each paired with a name from [PrestoSelf] or `transition(key)`. */
object Presto {
    val Height = Css.ViewTransitionClass.of(PrestoKey.Height)
    val Card = Css.ViewTransitionClass.of(PrestoKey.Card)
    val Pill = Css.ViewTransitionClass.of(PrestoKey.Pill)
}

object PrestoKey {
    const val Height = "presto-height"
    const val Card = "presto-card"
    const val Pill = "presto-pill"
}

//language="CSS"
val PrestoCss get() = with(PrestoKey) { """

/* an element that survives a change, paired with itself */
$PrestoSelf { view-transition-name: match-element; }

/* the snapshots fill the animating box, clipped by it and anchored to its top */
::view-transition-old(*.$Height),
::view-transition-new(*.$Height) {
    height: 100%;
    object-fit: none;
    object-position: top;
    overflow: clip;
}

/* the card's box animates with true corners, clipping snapshots kept at their natural size */
::view-transition-group(*.$Card) {
    border-radius: calc(var(--curve-unit) * 2);
    overflow: clip;
}

::view-transition-old(*.$Card),
::view-transition-new(*.$Card) {
    width: 100%;
    height: 100%;
    object-fit: none;
    object-position: top left;
}

/* the pill's box animates with true ends, clipping snapshots kept at their natural size; anchored left, they ride
   the group's transform rather than a centering offset that snaps to whole pixels */
::view-transition-group(*.$Pill) {
    border-radius: 9999px;
    overflow: clip;
}

::view-transition-old(*.$Pill),
::view-transition-new(*.$Pill) {
    width: 100%;
    height: 100%;
    object-fit: none;
    object-position: left;
}

"""}
