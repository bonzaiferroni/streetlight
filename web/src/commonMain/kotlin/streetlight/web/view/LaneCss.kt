package streetlight.web.view

import koala.modifier.*

object LaneStyle {
    val Row = Class("lane-row")
    val Content = Class("lane-content")
    val Slot = Content.withBemElement("slot")
    val SlotLabel = Slot.withBemElement("label")
    val LeadIn = Content.withBemElement("lead-in")
    val RepeatSlot = Slot.withBemModifier("repeat")
    val CursorOverlay = Class("lane-cursor-overlay")
}

//language="CSS"
val LaneCss get() = with(LaneStyle) { """

$Row { display: grid; grid-template-columns: minmax(0, 1fr); }

$Content { position: relative; overflow: clip; }

$Slot {
    position: absolute;
    inset-block: 0;
}

/* below the height of the labels, level with the measures of the slots */
$LeadIn {
    position: absolute;
    inset-block: var(--unit-3) 0;
}

$SlotLabel {
    height: var(--unit-3);
    padding-inline: var(--unit-1);
    overflow: clip;
    white-space: nowrap;
    text-transform: uppercase;
    opacity: 0.5;
}

$RepeatSlot { opacity: 0.6; }

$CursorOverlay {
    position: absolute;
    inset: 0;
    overflow: clip;
    pointer-events: none;
}

/* the meta panel moves beside the lane where there is room for it */
@media (min-width: 600px) {
    $Row {
        grid-template-columns: var(--unit-16) minmax(0, 1fr);
        column-gap: var(--unit-1);
    }

    $CursorOverlay { inset-inline-start: calc(var(--unit-16) + var(--unit-1)); }
}
"""}
