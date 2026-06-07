package koala.model

import koala.css.Class
import koala.css.Modifier
import koala.html.GeoMapKey

enum class Altitude(override val identifier: String, val zoom: Double): Modifier {
    Kite("kite", 16.0),
    Raincloud("raincloud", 14.5),
    Airplane("airplane", 13.0),
    Astronaut("astronaut", 4.0),
    Comet("comet", Double.MAX_VALUE);

    override fun toString() = selector
}

fun altitudeOf(zoom: Double) = Altitude.entries.first { it.zoom < zoom }

// language="CSS"
val AltitudeCss get() = with(GeoMapKey) { """
$Window${Altitude.Raincloud}  ${MarkerClass.Base}${Altitude.Kite},
$Window${Altitude.Airplane}   ${MarkerClass.Base}${Altitude.Raincloud},
$Window${Altitude.Astronaut}  ${MarkerClass.Base}${Altitude.Airplane},
$Window${Altitude.Comet}      ${MarkerClass.Base}${Altitude.Astronaut} {
    width: 5px;
    height: 5px;
    border-radius: 50%;
    background-color: rgba(255, 255, 255, 0.65);
    animation: twinkle 2.4s ease-in-out infinite;
    animation-delay: var(--twinkle-delay);
    
    > * {
        opacity: 0;    
    }
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