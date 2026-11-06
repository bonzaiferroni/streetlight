package streetlight.web.view

import koala.modifier.*

object LaneStyle {
    val Row = Class("lane-row")
    val Lane = Class("lane")
    val Selected = Row.withBemModifier("selected")
    val Meta = Row.withBemElement("meta")
    val Content = Class("lane-content")
    val Slot = Content.withBemElement("slot")
    val SlotLabel = Slot.withBemElement("label")
    val LeadIn = Content.withBemElement("lead-in")
    val RepeatSlot = Slot.withBemModifier("repeat")
    val CursorOverlay = Class("lane-cursor-overlay")
    val NoteCursor = Content.withBemElement("note-cursor")
}

//language="CSS"
val LaneCss get() = with(LaneStyle) { """

$Row { display: grid; grid-template-columns: minmax(0, 1fr); }

$Meta {
    padding: var(--unit-1) var(--unit-1) 0;
    text-align: center;
}

$Content { position: relative; overflow: clip; }

/* the outline is painted with the background, so the staff draws over it and it takes no room from the timeline's scale */
$Lane {
    border-radius: var(--unit-2);
    box-shadow: inset 0 0 0 1px rgba(var(--ink), var(--opacity-ghost));
}

$Selected $Lane { background: var(--primary-card-bg); }

$Selected $Meta { color: var(--primary-fg); }

/* only a selected lane's notes take the pointer, so a touch anywhere else still scrolls */
$Selected ${MeasureStyle.Grabbable} {
    cursor: grab;
    touch-action: none;
}

/* a unit of room above the label, which with the label's height makes the three units a lane keeps above its staff */
$Slot {
    position: absolute;
    inset-block: var(--unit-1) 0;
}

/* below the height of the labels, level with the measures of the slots */
$LeadIn {
    position: absolute;
    inset-block: var(--unit-3) 0;
}

$SlotLabel {
    height: var(--unit-2);
    line-height: 1;
    padding-inline: var(--unit-1);
    overflow: clip;
    white-space: nowrap;
    text-transform: uppercase;
    opacity: 0.5;
}

$RepeatSlot { opacity: 0.6; }

/* above the slots, and clear of the pointer so a drag stays on its note */
$NoteCursor {
    position: absolute;
    inset: 0;
    pointer-events: none;
    --note-ink: var(--primary-fg);
}

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

    $Meta {
        padding: var(--unit-1);
        text-align: start;
    }

    $CursorOverlay { inset-inline-start: calc(var(--unit-16) + var(--unit-1)); }
}
"""}
