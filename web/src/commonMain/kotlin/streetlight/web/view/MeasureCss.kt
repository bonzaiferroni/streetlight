package streetlight.web.view

import koala.modifier.*

object MeasureStyle {
    val Strip = Class("measure-strip")
    val Measure = Strip.withBemElement("measure")
    val Note = Strip.withBemElement("note")
    val OpenNote = Note.withBemModifier("open")
    val StemNote = Note.withBemModifier("stem")
    val OneFlagNote = Note.withBemModifier("one-flag")
    val TwoFlagNote = Note.withBemModifier("two-flag")
    val Dot = Strip.withBemElement("dot")
}

//language="CSS"
val MeasureCss get() = with(MeasureStyle) { """

$Strip {
    display: flex;
    border-inline-end: 2px solid rgba(var(--ink), 0.5);
}

$Measure {
    position: relative;
    border-inline-start: 2px solid rgba(var(--ink), 0.5);
}

/* a head as tall as a staff space, leaning as an engraved one does */
$Note {
    position: absolute;
    width: calc(var(--unit) * 2.8);
    height: var(--unit-2);
}

$Note::before {
    content: "";
    position: absolute;
    inset: 0;
    border-radius: 50%;
    rotate: -20deg;
    background: rgb(var(--ink));
}

$OpenNote::before {
    background: none;
    border: 1.5px solid rgb(var(--ink));
}

/* the stem, kept to the height of the head */
$StemNote::after, $OneFlagNote::after, $TwoFlagNote::after {
    content: "";
    position: absolute;
    inset-block: 0;
    inset-inline-start: 100%;
    width: var(--unit-1);
    border-inline-start: 1.5px solid rgb(var(--ink));
}

$OneFlagNote::after, $TwoFlagNote::after { border-block-start: 1.5px solid rgb(var(--ink)); }
$TwoFlagNote::after { border-block-end: 1.5px solid rgb(var(--ink)); }

$Dot {
    position: absolute;
    inset-inline-start: calc(100% + var(--unit-2));
    top: 50%;
    width: 6px;
    aspect-ratio: 1;
    translate: 0 -50%;
    border-radius: 50%;
    background: rgb(var(--ink));
}
"""}
