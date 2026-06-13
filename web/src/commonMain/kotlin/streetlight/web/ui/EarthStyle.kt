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

object EarthStyle {
    val Id = Id("earth")
    val ViewMapButtonMod = modify(PositionSticky, TopSpacing1, JustifySelfCenter, AlignSelfStart, ZIndex1)
    val IsMoving = Class("is-moving")
    val MoveDimmer = Class("move-dimmer")
    val IsFocused = Class("is-focused")
    val ListDetail = Class("list-detail")

    val Grid = Class("earth-grid")
    val Map = Class("earth-map")
    val Header = Class("earth-header")
    val Window = Class("earth-window")
    val Unbounded = Class("earth-unbounded")
    val Focus = Class("earth-focus")

    val MinifiedWidth = 800
}

// language="CSS"
val EarthCss get() = with(EarthStyle) { """

${EarthStyle.Id} .maplibregl-ctrl-top-right {
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
    height: 100vh;
    
    > * {
        height: 100vh;
    }

    $MoveDimmer {
        transition: var(--transition-opacity);
        opacity: 1;
    }
    
    &$IsMoving {
        $MoveDimmer {
            opacity: .6;
        }
    }
    
    > * {
        isolation: isolate;
    }
}

$Grid {
    display: grid;
    grid-template-columns: auto;
    grid-template-rows: 1fr 300px;
    grid-template-areas: 
        "window"
        "focus";
    gap: var(--unit-spacing);

    > $Window    { grid-area: window; }
    > $Focus     { grid-area: focus; max-width: 400px; }
    > $Unbounded { grid-column: 1 / -1; grid-row: 1 / -1; }
    
    $ListDetail {
        width: 400px;
        transition: width var(--magic-interval) var(--magic-easing);
        overflow: hidden;
    }
    
    @media (max-width: ${MinifiedWidth}px) {
        &$IsFocused { 
            $ListDetail {
                width: 0;
            }
        }
    }
    
    @media (min-width: ${MinifiedWidth}px) {
        grid-template-columns: 400px 1fr;
        grid-template-rows: auto;
        grid-template-areas: "focus window";
    }
}

$Unbounded {
    position: relative;
    
    > * {
        position: absolute;
    }
}

""" }