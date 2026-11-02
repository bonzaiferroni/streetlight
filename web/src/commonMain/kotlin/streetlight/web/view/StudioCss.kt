package streetlight.web.view

import koala.modifier.*

object StudioStyle {
    val Track = Class("studio-track")
    val Timeline = Class("studio-timeline")
    val CursorHandle = Timeline.withBemElement("cursor-handle")
    val Lane = Class("studio-lane")
    val Waveform = Lane.withBemElement("waveform")
    val CursorLine = Lane.withBemElement("cursor-line")
}

//language="CSS"
val StudioCss get() = with(StudioStyle) { """
    
$Timeline { position: relative; touch-action: none; }
$Lane     { position: relative; }
$Waveform { display: block; height: 100%; }

$Track {
    display: grid;
    grid-template-columns: var(--unit-16) minmax(0, 1fr);
    column-gap: var(--unit-1);
}

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
