package koala.model

import koala.Asset
import koala.modifier.*
import koala.html.IconStyle
import kotlinx.css.LinearDimension
import kotlinx.css.properties.Angle

object MarkerStyle {
    val Root = Class("map-marker")
    val Base = Class("marker-base")
    val Bearing = Class("marker-bearing")
    val Travel = Class("marker-travel")
    val Thumb = Class("marker-thumb")
    val Icon = Class("marker-icon")
    val Body = Class("marker-body")
    val Label = Class("marker-label")

    val BodySize = Property<LinearDimension>("body-size", true)
    val MarkerSvg = Property<Asset>("marker-svg", true)
    val MarkerBearing = Property<Angle>("marker-bearing", true)
    val MarkerBorder = Property<Rgb>("marker-border", true)

    val LabelMod = modify(Bold, MaxWidth(28), TextOverflowEllipses, TextShadow)
}

// language="CSS"
val MarkerSheet get() = with(MarkerStyle) { """
    
:root {
    --map-text-shadow: 0 1px 2px rgba(var(--paper), .2), 0 0 12px rgba(var(--paper), 1);
}
    
/* Focus Properties */
$Root$Focus {
    z-index: 1;
        
    $Base$Scale {
        transform: scale(1.5);            
    }
    
    $Thumb {
        border-radius: 4px;
    }
    
    $Label {
        opacity: 1;
    }
}

$Base {
    cursor: pointer;
    position: relative;
    width: 0;
    height: 0;

    transition: var(--transition-transform);
    text-shadow: var(--map-text-shadow);
    
    > * {
        position: absolute;
        top: 0;
        left: 0;    
    }
    
    &:hover {
        opacity: 1 !important;
    }
}

/* Turns the marker to its bearing, its icon drawn pointing up */
$Base$Bearing {
    rotate: var($MarkerBearing);
    transition: var(--transition-transform), rotate 1000ms ease-out;
}

$Body {
    transform: translate(calc(var($BodySize) / -2), calc(var($BodySize) / -2));
    opacity: 1;
    transition: var(--transition-opacity);
    font-size: 1rem;
    
   @starting-style {
        opacity: 0;
    }
}

$Travel {
    display: grid;
    place-items: center;
    width: var($BodySize);
    height: var($BodySize);
}

$Thumb {
    $MarkerBorder: 255, 255, 255; 

    width: var($BodySize);
    height: var($BodySize);
    max-width: none;

    border-radius: calc(var($BodySize) / 2);
    overflow: clip;

    border: 2px solid rgba(var($MarkerBorder), 0.6);

    transition: var(--transition-opacity), var(--transition-border-radius);
}

$Icon {
    width: var($BodySize);
    height: var($BodySize);
    
    transition: var(--transition-opacity), var(--transition-border-radius);
}

$Label {
    transform: translate(-50%, calc(50% + .1rem)); /* below body */
    pointer-events: none;
    opacity: 0;
    font-weight: 700;

    max-width: 6rem;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;

    transition: var(--transition-opacity);
}

""" }