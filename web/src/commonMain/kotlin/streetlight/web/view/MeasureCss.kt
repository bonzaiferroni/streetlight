package streetlight.web.view

import koala.modifier.*

/** The height of a staff step in units, two to a staff space. */
const val STAFF_STEP_UNITS = 1.2

object MeasureStyle {
    val Strip = Class("measure-strip")
    val Staff = Strip.withBemElement("staff")
    val Bar = Strip.withBemElement("bar")
    val Measure = Strip.withBemElement("measure")
    val Note = Strip.withBemElement("note")
    val OpenNote = Note.withBemModifier("open")
    val StemNote = Note.withBemModifier("stem")
    val OneFlagNote = Note.withBemModifier("one-flag")
    val TwoFlagNote = Note.withBemModifier("two-flag")
    val Dot = Strip.withBemElement("dot")
    val Ledger = Strip.withBemElement("ledger")
    val Accidental = Strip.withBemElement("accidental")
    val Grabbable = Note.withBemModifier("grabbable")
    val ControlNote = Note.withBemModifier("control")
}

//language="CSS"
val MeasureCss get() = with(MeasureStyle) { """

$Strip {
    position: relative;
    display: flex;
}

/* five lines a staff space apart, faded as one so they do not darken where the bars cross them */
$Staff {
    position: absolute;
    inset-inline: 0;
    height: calc(var(--unit) * ${STAFF_STEP_UNITS * 8} + 2px);
    opacity: var(--opacity-low);
    overflow: clip;
    border-inline-end: 2px solid rgb(var(--ink));
    background: repeating-linear-gradient(to top, rgb(var(--ink)) 0 2px, transparent 2px calc(var(--unit) * ${STAFF_STEP_UNITS * 2}));
}

/* centered on the downbeat, so a section's edges each keep half a bar and two sections meet in a whole one */
$Bar {
    position: absolute;
    inset-block: 0;
    width: 4px;
    translate: -2px 0;
    background: rgb(var(--ink));
}

$Measure { position: relative; }

/* centered on its onset, so it sounds as the cursor meets its middle, and on its step */
$Note {
    position: absolute;
    width: calc(var(--unit) * 2.4);
    height: var(--unit-2);
    translate: -50% 50%;
    border-radius: 50%;
    background: var(--note-ink, rgb(var(--ink)));
}

$OpenNote { background: none; }

/* a border thicker at the sides swells the stroke there, and a border on the head itself would move its bars */
$OpenNote::before {
    content: "";
    position: absolute;
    inset: 0;
    border-radius: 50%;
    border-style: solid;
    border-color: var(--note-ink, rgb(var(--ink)));
    border-width: 2px 4px;
}

/* one bar through the head, painted only where it shows above and below, which leaves an open head clear */
$StemNote::after, $OneFlagNote::after, $TwoFlagNote::after {
    content: "";
    position: absolute;
    inset-block: -4px;
    inset-inline-start: 50%;
    width: 2px;
    translate: -50% 0;
    background: linear-gradient(
        var(--note-ink, rgb(var(--ink))) 4px,
        transparent 4px calc(100% - 4px),
        var(--note-ink, rgb(var(--ink))) calc(100% - 4px)
    );
}

/* a flag centered on each end of the bar, wider for the shorter value */
$OneFlagNote::before, $TwoFlagNote::before {
    content: "";
    position: absolute;
    inset-block: -4px;
    inset-inline-start: 50%;
    width: 8px;
    translate: -50% 0;
    border-block: 2px solid var(--note-ink, rgb(var(--ink)));
}

$TwoFlagNote::before { width: 12px; }

$Ledger {
    position: absolute;
    inset-inline: -4px;
    height: 2px;
    background: rgb(var(--ink));
    opacity: var(--opacity-low);
}

/* a note standing alone in a control, off the staff */
$ControlNote {
    position: relative;
    translate: none;
    margin: var(--unit-1) var(--unit-2);
}

$Accidental {
    position: absolute;
    inset-inline-end: 100%;
    top: 50%;
    translate: 0 -50%;
    line-height: 1;
    color: var(--note-ink, rgb(var(--ink)));
}

$Dot {
    position: absolute;
    inset-inline-start: calc(100% + var(--unit-1) / 2);
    top: 50%;
    width: 6px;
    aspect-ratio: 1;
    translate: 0 -50%;
    border-radius: 50%;
    background: var(--note-ink, rgb(var(--ink)));
}
"""}
