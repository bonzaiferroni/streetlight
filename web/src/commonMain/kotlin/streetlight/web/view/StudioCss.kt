package streetlight.web.view

import koala.modifier.*

object StudioStyle {
    val Timeline = Class("studio-timeline")
    val CursorHandle = Timeline.withBemElement("cursor-handle")
    val Sample = Class("studio-sample")
    val Waveform = Sample.withBemElement("waveform")
    val CursorLine = Sample.withBemElement("cursor-line")
}

//language="CSS"
val StudioCss get() = with(StudioStyle) { """
$Timeline { position: relative; touch-action: none; }
$Sample   { position: relative; }
$Waveform { display: block; width: 100%; height: 100%; }

$CursorHandle {
    position: absolute;
    top: 50%;
    width: var(--unit-2);
    aspect-ratio: 1;
    translate: -50% -50%;
    border-radius: 50%;
    background: var(--accent-fg);
    cursor: grab;
}

$CursorLine {
    position: absolute;
    inset-block: 0;
    width: 1px;
    background: var(--accent-fg);
}
"""}
