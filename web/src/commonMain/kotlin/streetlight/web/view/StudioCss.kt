package streetlight.web.view

import koala.modifier.*

object StudioStyle {
    val Track = Class("studio-track")
    val Timeline = Class("studio-timeline")
    val WindowRegion = Timeline.withBemElement("window-region")
    val WindowEdge = Timeline.withBemElement("window-edge")
    val CursorHandle = Timeline.withBemElement("cursor-handle")
    val Lane = Class("studio-lane")
    val Extent = Lane.withBemElement("extent")
    val Waveform = Lane.withBemElement("waveform")
    val Stage = Class("studio-stage")
    val CursorOverlay = Stage.withBemElement("cursor-overlay")
    val CursorLine = Stage.withBemElement("cursor-line")
}

//language="CSS"
val StudioCss get() = with(StudioStyle) { """
    
$Timeline { position: relative; touch-action: none; min-height: var(--unit-4); }
$Lane     { position: relative; touch-action: pan-y; }
$Extent   { position: absolute; inset-block: 0; }

$Waveform {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
}

$WindowRegion {
    position: absolute;
    inset-block: 0;
    background: color-mix(in srgb, var(--accent-fg) 25%, transparent);
    cursor: grab;
}

$WindowEdge {
    position: absolute;
    inset-block: 0;
    inset-inline-end: 0;
    width: var(--unit-2);
    background: var(--accent-fg);
    cursor: ew-resize;
}

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
    background: var(--primary-fg);
    cursor: grab;
}

$Stage { position: relative; }

/* over the lane column of every track, past its panel and gap */
$CursorOverlay {
    position: absolute;
    inset-block: 0;
    inset-inline: calc(var(--unit-16) + var(--unit-1)) 0;
    overflow: clip;
    pointer-events: none;
}

$CursorLine {
    position: absolute;
    inset-block: 0;
    width: 1px;
    background: var(--primary-fg);
}
"""}
