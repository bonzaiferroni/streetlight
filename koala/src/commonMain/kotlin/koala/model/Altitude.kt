package koala.model

import koala.css.Modifier
import koala.html.GeoMapKey

enum class Altitude(override val identifier: String, val zoom: Double): Modifier {
    Kite("kite", 16.0),
    Raincloud("raincloud", 14.5),
    Airplane("airplane", 13.0),
    Satellite("satellite",8.0),
    Astronaut("astronaut", 4.0),
    Comet("comet", Double.MAX_VALUE);

    override fun toString() = selector
}

fun altitudeOf(zoom: Double) = Altitude.entries.first { it.zoom < zoom }

// language="CSS"
val AltitudeCss get() = with(GeoMapKey) { """
    
/* diminished visibility for markers just below altitude */
$Window${Altitude.Raincloud}  ${MarkerMod.Base}${Altitude.Kite},
$Window${Altitude.Airplane}   ${MarkerMod.Base}${Altitude.Raincloud},
$Window${Altitude.Satellite}  ${MarkerMod.Base}${Altitude.Airplane},
$Window${Altitude.Astronaut}  ${MarkerMod.Base}${Altitude.Satellite},
$Window${Altitude.Comet}      ${MarkerMod.Base}${Altitude.Astronaut} {
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
$Window${Altitude.Airplane}   ${MarkerMod.Base}${Altitude.Kite},
$Window${Altitude.Satellite}  ${MarkerMod.Base}${Altitude.Raincloud},
$Window${Altitude.Astronaut}  ${MarkerMod.Base}${Altitude.Airplane},
$Window${Altitude.Comet}      ${MarkerMod.Base}${Altitude.Satellite} {
    display: none;
}

@keyframes twinkle {
    0%, 100% {
        background-color: rgba(255, 255, 255, 0.45);
    }
    50% {
        background-color: rgba(255, 255, 255, 1);
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