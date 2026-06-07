package koala.model

import koala.css.Class
import koala.css.Focus
import koala.css.Scale

object MarkerClass {
    val Root = Class("map-marker")
    val Base = Class("map-marker__base")
    val Bearing = Class("map-marker__bearing")
    val Icon = Class("map-marker__icon")
    val Thumb = Class("map-marker__thumb")
    val Body = Class("map-marker__body")
    val Label = Class("map-marker__label")
}

// language="CSS"
val MarkerElementCss get() = with(MarkerClass) { """
    
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

    transition: transform 200ms ease-in-out;
    
    > * {
        position: absolute;
        top: 0;
        left: 0;    
    }
    
    &$Scale {
        transform: scale(1);    
    }
    
    &:hover {
        opacity: 1 !important;
    }
}

$Body {
    transform: translate(-50%, -50%);
    opacity: 1;
    transition: opacity 200ms ease-in-out;
}

$Icon {
    width: 24px;
    height: 24px;

    background-image: var(--svg);
    background-size: contain;
    background-repeat: no-repeat;
    background-position: center;
    background-color: transparent; /* important */;
}

$Bearing {
    width: 40px;
    height: 40px;

    transform: translate(-50%, -50%) rotate(var(--bearing));

    background-image: url(/www/svg/bus-direction.svg);
    background-size: contain;
    background-repeat: no-repeat;
    background-position: center;
    background-color: transparent; /* important */

    transition: transform 1000ms ease-out;
}

$Thumb {
    --thumb-border-rgb: 255, 255, 255; /* or whatever ye like */

    width: 32px;
    height: 32px;
    max-width: none;

    border-radius: 16px;

    border: 2px solid rgba(var(--thumb-border-rgb), 0.8);

    transition: opacity 200ms ease-in-out, border-radius 200ms ease-in-out;
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

    transition: opacity 200ms ease-in-out;
}
""" }