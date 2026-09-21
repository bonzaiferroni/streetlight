package streetlight.web.ui

import koala.modifier.*
import koala.html.Id

object EarthStyle {
    val Container = Id("earth")
    val ViewMapButtonMod = modify(PositionSticky, Css.Top(1), JustifySelfCenter, AlignSelfStart, ZIndex1)
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
    val MapTitle = Class("earth-map-title")

    val MinifiedWidth = 800
}

// language="CSS"
val EarthCss get() = with(EarthStyle) { """

${EarthStyle.Container} .maplibregl-ctrl-top-right {
    top: 50%;
    transform: translateY(-50%);
}

:root:not($DayTheme) .maplibregl-ctrl {
    filter: invert(1);
}

.maplibregl-canvas {
    transition: var(--transition-filter);
}

$DayTheme .maplibregl-canvas {
    filter: invert(1) hue-rotate(180deg);
}

$Container {
    height: 100dvh;
    
    > * {
        height: 100dvh;
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
    grid-template-rows: auto 1fr minmax(0, 2fr);
    grid-template-areas: 
        "header"
        "window"
        "focus";

    > $Header    { grid-area: header; }
    > $Window    { grid-area: window; }
    > $Focus     { grid-area: focus; max-width: 600px; align-self: end; }
    > $Unbounded { grid-area: 1 / 1 / -1 / -1; }
    
    $ListDetail {
        width: 400px;
        transition: var(--transition-width);
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
        grid-template-columns: 600px 1fr;
        grid-template-rows: auto 1fr;
        grid-template-areas:
            "header header"
            "focus window";
    }
}

$Unbounded {
    position: relative;
    
    > * {
        position: absolute;
    }
}

$Header {
    text-shadow: var(--map-text-shadow);
    z-index: 1; /* not ideal */
}

$MapTitle {
    background: linear-gradient(to right, transparent 0%, rgba(var(--paper), .6) 50%, transparent 100%);
}

""" }