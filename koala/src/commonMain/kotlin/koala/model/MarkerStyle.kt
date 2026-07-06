package koala.model

import koala.SiteFile
import koala.css.Class
import koala.css.Focus
import koala.css.Property
import koala.css.Rgb
import koala.css.Scale
import kotlinx.css.Color
import kotlinx.css.LinearDimension
import kotlinx.css.properties.Angle
import kotlinx.css.properties.Time

object MarkerStyle {
    val Root = Class("map-marker")
    val Base = Class("marker-base")
    val Bearing = Class("marker-bearing")
    val Travel = Class("marker-travel")
    val Thumb = Class("marker-thumb")
    val Icon = Class("marker-icon")
    val Body = Class("marker-body")
    val Label = Class("marker-label")
    val ClusterCount = Class("marker-cluster-count")

    val ClusterPrincipal = Class("cluster-principal")
    val ClusterMember = Class("cluster-member")
    val MarkerGlow = Class("marker-glow")

    val BodySize = Property<LinearDimension>("body-size")
    val TwinkleDelay = Property<Time>("twinkle-delay")
    val MarkerLight = Property<Color>("marker-light")
    val MarkerSvg = Property<SiteFile>("marker-svg")
    val MarkerBearing = Property<Angle>("marker-bearing")
    val MarkerBorder = Property<Rgb>("marker-border")
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
    
    p {
        font-size: 1rem;
    }
}

$Body {
    transform: translate(calc(var($BodySize) / -2), calc(var($BodySize) / -2));
    opacity: 1;
    transition: var(--transition-opacity);
    
   @starting-style {
        opacity: 0;
    }
}

$Travel {
    position: relative;
    width: var($BodySize);
    height: var($BodySize);

    background-image: var($MarkerSvg);
    background-size: contain;
    background-repeat: no-repeat;
    background-position: center;
    background-color: transparent; 
}

$Bearing {
    width: calc(var($BodySize) + 16px);
    height: calc(var($BodySize) + 16px);

    transform: translate(-50%, -50%) rotate(var($MarkerBearing));

    background-image: url(/www/svg/bus-direction.svg);
    background-size: contain;
    background-repeat: no-repeat;
    background-position: center;
    background-color: transparent; 

    transition: transform 1000ms ease-out;
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

$ClusterMember {
    $Body {
        opacity: 0;
    }
    
    &$Root {
        pointer-events: none;
    }
}

$ClusterCount {
    opacity: 0;
    
    transition: var(--transition-opacity);
}

$ClusterPrincipal {
    $ClusterCount {
        opacity: 1;
    }
    
    .icon {
        opacity: 0;
    }
}

$MarkerGlow::before {
    content: "";
    position: absolute;

    left: 50%;
    top: 50%;
    width: 6rem;
    height: 6rem;

    transform: translate(-50%, -50%) scale(1);
    border-radius: 50%;

    pointer-events: none;
    mix-blend-mode: screen;

    opacity: 0.5;

    background: radial-gradient(
            circle at center,
            rgba(var($MarkerLight), 0.35) 0%,
            rgba(var($MarkerLight), 0.18) 12%,
            rgba(var($MarkerLight), 0.08) 24%,
            rgba(var($MarkerLight), 0) 70%
    );

    transition: var(--transition-opacity), var(--transition-transform);

    animation: twinkle-scale 2.4s ease-in-out infinite;
    animation-delay: var($TwinkleDelay, 0s);
}

/* brighter + slightly larger when focused */
$MarkerGlow$Focus::before {
    opacity: 0.75;
    transform: translate(-50%, -50%) scale(1.05);
}

""" }