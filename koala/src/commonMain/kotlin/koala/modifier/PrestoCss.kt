@file:Suppress("CssInvalidHtmlTagReference")

package koala.modifier

val PrestoSelf = Class("presto-self")
val PrestoItems = Class("presto-items")

/** The view transition classes, each paired with a name from [PrestoSelf] or `transition(key)`. */
object Presto {
    val Height = Css.ViewTransitionClass.of(PrestoKey.Height)
    val Card = Css.ViewTransitionClass.of(PrestoKey.Card)
    val Pill = Css.ViewTransitionClass.of(PrestoKey.Pill)
    val Pop = Css.ViewTransitionClass.of(PrestoKey.Pop)
    val Raised = Css.ViewTransitionClass.of(PrestoKey.Raised)
}

object PrestoKey {
    const val Height = "presto-height"
    const val Card = "presto-card"
    const val Pill = "presto-pill"
    const val Pop = "presto-pop"
    const val Raised = "presto-raised"
}

//language="CSS"
val PrestoCss get() = with(PrestoKey) { """

/* an element that survives a change, paired with itself */
$PrestoSelf { view-transition-name: match-element; }

/* each direct child that survives a change, paired with itself, so items move as their siblings change size */
$PrestoItems > * { view-transition-name: match-element; }

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

/* an element on one side of the change only, its snapshot alone in its pair, pops in or out */
::view-transition-new(*.$Pop):only-child { animation: presto-pop-in var(--magic-interval) var(--magic-cubic-bounce); }
::view-transition-old(*.$Pop):only-child { animation: presto-pop-out var(--magic-interval) var(--magic-easing) forwards; }

@keyframes presto-pop-in  { from { opacity: 0; transform: scale(${MagicStyle.InitialScale}); } }
@keyframes presto-pop-out { to   { opacity: 0; transform: scale(${MagicStyle.InitialScale}); } }

/* drawn above the other named elements while they move, as a label over a light that slides beneath it */
::view-transition-group(*.$Raised) { z-index: 1; }

"""}
