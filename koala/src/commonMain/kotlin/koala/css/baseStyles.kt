package koala.css

import kotlinx.css.*
import kotlinx.css.properties.Animation
import kotlinx.css.properties.Animations
import kotlinx.css.properties.IterationCount
import kotlinx.css.properties.LineHeight
import kotlinx.css.properties.RadialGradientShape
import kotlinx.css.properties.Timing
import kotlinx.css.properties.Transition
import kotlinx.css.properties.radialGradient
import kotlinx.css.properties.s

fun CssBuilder.baseStyles(theme: KoalaTheme) {

    html {
        fontSize = 100.pct
    }

    body {
        fontFamily = "system-ui, -apple-system, Segoe UI, Roboto, sans-serif"
        fontSize = 1.rem
        lineHeight = LineHeight("1.5")
        margin = Margin(LinearDimension("0"), LinearDimension.auto)
        padding = Padding(theme.spacingUnit)
        backgroundColor = theme.bg
        color = theme.fg
        position = Position.relative
        minHeight = 100.vh
        maxWidth = 960.px
    }

//    "body:before" {
//        content = QuotedString("")
//        position = Position.fixed
//        inset = Inset(LinearDimension("0"))
//        pointerEvents = PointerEvents.none
//        zIndex = -1
//        val g1: Image = radialGradient {
//            circle()
//            at(RelativePosition.offset(18.pct, 18.pct))
//            colorStop(theme.light1.changeAlpha(.2), 0.pct)
//            colorStop(Color.transparent, 50.pct)
//        }
//
//        val g2: Image = radialGradient {
//            circle()
//            at(RelativePosition.offset(82.pct, 16.pct))
//            colorStop(theme.light2.changeAlpha(.2), 0.pct)
//            colorStop(Color.transparent, 48.pct)
//        }
//
//        val g3: Image = radialGradient {
//            circle()
//            at(RelativePosition.offset(50.pct, 42.pct))
//            colorStop(theme.light3.changeAlpha(.2), 0.pct)
//            colorStop(Color.transparent, 55.pct)
//        }
//        background = "$g1,\n$g2,\n$g3"
//        filter = "hue-rotate(0deg)"
//        animation += Animation("hueSpin", 30.s, Timing.linear, iterationCount = IterationCount.infinite)
//        willChange = "filter"
//    }

//    "input[type='text'], input[type='password']" {
//        border = Border.none
//        outline = Outline.none
//        padding = Padding(0.75.rem)
//        borderRadius = 0.75.rem
//        fontSize = 1.2.rem
//        backgroundColor = theme.void
//        color = theme.fg
//    }

//    "input[type=\"checkbox\"]" {
//        backgroundColor = theme.void
//        width = 1.5.rem
//        height = 1.5.rem
//        borderRadius = 0.25.rem
//    }

    keyframes("hueSpin") {
        to {
            filter = "hue-rotate(360deg)"
        }
    }

    a {
        transition += Transition("color", .3.s, Timing.ease)
    }

    "p a" {
        color = theme.primary
    }

    "a:hover" {
        color = theme.accent
        animation += Animation("glow", 10.s, Timing.linear, iterationCount = IterationCount.infinite)
    }
}