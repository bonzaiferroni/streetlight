package koala.model

import koala.css.Class

enum class Altitude(val selector: String) {
    Kite("kite"),
    Raincloud("raincloud"),
    Airplane("airplane"),
    Astronaut("astronaut"),
    Comet("comet");

    val css = Class(selector)
    val aboveCss = Class("above-$selector")
    val belowCss = Class("below-$selector")
    val modifiers by lazy {
        entries.map {
            if (it < this) it.aboveCss
            else if (it > this) it.belowCss
            else it.css
        }.toSet()
    }
}

fun altitudeOf(zoom: Double): Altitude =
    when {
        zoom >= 16 -> Altitude.Kite
        zoom >= 14.5 -> Altitude.Raincloud
        zoom >= 13  -> Altitude.Airplane
        zoom >= 4  -> Altitude.Astronaut
        else       -> Altitude.Comet
    }