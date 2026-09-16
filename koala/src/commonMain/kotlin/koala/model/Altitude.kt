package koala.model

import koala.modifier.Class
import koala.html.GeoMapKey

enum class Altitude(val cssClass: Class, val zoom: Double) {
    Kite(Class("kite"), 16.0),
    Raincloud(Class("raincloud"), 14.5),
    Airplane(Class("airplane"), 13.0),
    Satellite(Class("satellite"),8.0),
    Astronaut(Class("astronaut"), 4.0),
    Comet(Class("comet"), Double.MIN_VALUE);

    override fun toString() = cssClass.selector
}

fun altitudeOf(zoom: Double) = Altitude.entries.first { it.zoom < zoom }

// language="CSS"
val AltitudeCss get() = with(GeoMapKey) { """
    
/* diminished visibility for markers just below altitude */
$Window${Altitude.Raincloud}  ${MarkerStyle.Base}${Altitude.Kite},
$Window${Altitude.Airplane}   ${MarkerStyle.Base}${Altitude.Raincloud},
$Window${Altitude.Satellite}  ${MarkerStyle.Base}${Altitude.Airplane},
$Window${Altitude.Astronaut}  ${MarkerStyle.Base}${Altitude.Satellite},
$Window${Altitude.Comet}      ${MarkerStyle.Base}${Altitude.Astronaut} {
    width: 5px;
    height: 5px;
    border-radius: 50%;
    background-color: rgba(255, 255, 255, 0.65);
    animation: twinkle 2.4s ease-in-out infinite;
    animation-delay: var(--twinkle-delay);
    pointer-events: none;
    
    > * {
        opacity: 0;    
    }
}

/* hide markers well below altitude */
$Window${Altitude.Airplane}   ${MarkerStyle.Base}${Altitude.Kite},
$Window${Altitude.Satellite}  ${MarkerStyle.Base}${Altitude.Raincloud},
$Window${Altitude.Astronaut}  ${MarkerStyle.Base}${Altitude.Airplane},
$Window${Altitude.Comet}      ${MarkerStyle.Base}${Altitude.Satellite} {
    display: none;
}

@keyframes twinkle {
    0%, 100% {
        background-color: rgba(255, 255, 255, 0.2);
    }
    50% {
        background-color: rgba(255, 255, 255, 0.6);
    }
}

@keyframes twinkle-scale {
    0%, 100% {
        transform: translate(-50%, -50%) scale(1);
    }
    50% {
        transform: translate(-50%, -50%) scale(1.3);
    }
}
""" }