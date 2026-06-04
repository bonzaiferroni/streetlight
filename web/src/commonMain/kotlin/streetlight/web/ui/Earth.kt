package streetlight.web.ui

import koala.css.AlignSelfStart
import koala.css.Class
import koala.css.DayTheme
import koala.css.JustifySelfCenter
import koala.css.PositionSticky
import koala.css.TopSpacing1
import koala.css.ZIndex1
import koala.css.modify
import koala.html.Id

object Earth {
    val Id = Id("earth")
    val ViewMapButtonMod = modify(PositionSticky, TopSpacing1, JustifySelfCenter, AlignSelfStart, ZIndex1)
    val IsMoving = Class("is-moving")
    val MoveDimmer = Class("move-dimmer")

    val Map = Class("earth-map")
    val Header = Class("earth-header")
    val Window = Class("earth-window")
    val Panel = Class("panel")

    val MinifiedWidth = 800
}

// language="CSS"
val EarthCss get() = with(Earth) { """

${Earth.Id} .maplibregl-ctrl-top-right {
    top: 50%;
    transform: translateY(-50%);
}

:root:not($DayTheme) .maplibregl-ctrl {
    filter: invert(1);
}

.maplibregl-canvas {
    transition: filter var(--magic-interval) var(--magic-easing);
}

$DayTheme .maplibregl-canvas {
    filter: invert(1) hue-rotate(180deg);
}

$Id {
    display: grid;
    grid-template-columns: auto;
    grid-template-rows: auto 1fr auto;
    grid-template-areas: 
        "header"
        "window"
        "panel";
    
    > $Header    { grid-area: header; }
    > $Window    { grid-area: window; }
    > $Panel     { grid-area: panel; }
    > $Map       { grid-column: 1 / -1; grid-row: 1 / -1; }
    
    $MoveDimmer {
        transition: var(--transition-opacity);
        opacity: 1;
    }
    
    &$IsMoving {
        $MoveDimmer {
            opacity: .75;
        }
    }
    
    @media (min-width: ${MinifiedWidth}px) {
        grid-template-columns: 400px 1fr;
        grid-template-rows: auto 1fr;
        grid-template-areas: 
            "header header"
            "panel window";
    }
}
""" }