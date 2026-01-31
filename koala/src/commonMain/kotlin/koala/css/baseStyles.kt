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
    // html {
    //    font-size: 100%;
    //}

    html {
        fontSize = 100.pct
    }

    // body {
    //    font-family: system-ui, -apple-system, "Segoe UI", Roboto, sans-serif;
    //    font-size: 1rem;
    //    line-height: 1.5;
    //    margin: 0 auto;
    //    padding: 0.5rem;
    //    background-color: rgb(var(--bg));
    //    color: rgb(var(--fg));
    //    position: relative;
    //    min-height: 100vh;
    //    max-width: 960px;
    //}

    body {
        fontFamily = "system-ui, -apple-system, \"Segoe UI\", Roboto, sans-serif"
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

    // body::before {
    //    content: "";
    //    position: fixed;
    //    inset: 0;
    //    pointer-events: none;
    //    z-index: -1;
    //    background:
    //            radial-gradient(circle at 18% 18%, rgba(var(--light-1), 0.2) 0%, transparent 50%),
    //            radial-gradient(circle at 82% 16%, rgba(var(--light-2), 0.2) 0%, transparent 48%),
    //            radial-gradient(circle at 50% 42%, rgba(var(--light-3), 0.2) 0%, transparent 55%);
    //    filter: hue-rotate(0deg);
    //    animation: hueSpin 30s linear infinite;
    //    will-change: filter;
    // }

    // &::before {
    //content: '';
    //position: fixed;
    //inset: 0;
    //pointer-events: none;
    //z-index: -1;
    //background: radial-gradient(circle at 18% 18%, rgba(255, 99, 132, 0.2) 0, transparent 50%),
    //radial-gradient(circle at 82% 16%, rgba(88, 164, 255, 0.2) 0, transparent 48%),
    //radial-gradient(circle at 50% 42%, rgba(88, 255, 188, 0.2) 0, transparent 55%);;
    //filter: hue-rotate(0deg);
    //animation: 30s linear 0s infinite normal none running hueSpin;
    //will-change: filter;
    //}
    //}

    "body::before" {
        before {
            content = QuotedString("")
            position = Position.fixed
            inset = Inset(LinearDimension("0"))
            pointerEvents = PointerEvents.none
            zIndex = -1
            val g1: Image = radialGradient {
                circle()
                at(RelativePosition.offset(18.pct, 18.pct))
                colorStop(theme.light1.changeAlpha(.2), 0.pct)
                colorStop(Color.transparent, 50.pct)
            }

            val g2: Image = radialGradient {
                circle()
                at(RelativePosition.offset(82.pct, 16.pct))
                colorStop(theme.light2.changeAlpha(.2), 0.pct)
                colorStop(Color.transparent, 48.pct)
            }

            val g3: Image = radialGradient {
                circle()
                at(RelativePosition.offset(50.pct, 42.pct))
                colorStop(theme.light3.changeAlpha(.2), 0.pct)
                colorStop(Color.transparent, 55.pct)
            }
            background = "$g1,\n$g2,\n$g3"
            filter = "hue-rotate(0deg)"
            animation += Animation("hueSpin", 30.s, Timing.linear, iterationCount = IterationCount.infinite)
            willChange = "filter"
        }
    }

    // input[type="text"] {
    //    border: none;
    //    outline: none;
    //    padding: 0.75rem;
    //    border-radius: 0.75rem;
    //    font-size: 1.2rem;
    //    background-color: rgb(var(--void));
    //    color: rgb(var(--fg));
    //}

    "input[type=\"text\"]" {
        border = Border.none
        outline = Outline.none
        padding = Padding(0.75.rem)
        borderRadius = 0.75.rem
        fontSize = 1.2.rem
        backgroundColor = theme.void
        color = theme.fg
    }

    // input[type="checkbox"] {
    //background-color: rgb(24, 31, 31);
    //width: 1.5rem;
    //height: 1.5rem;
    //border-radius: 0.25rem;
    //}

    "input[type=\"checkbox\"]" {
        backgroundColor = theme.void
        width = 1.5.rem
        height = 1.5.rem
        borderRadius = 0.25.rem
    }

    // @keyframes hueSpin {
    //    to { filter: hue-rotate(360deg); }
    //}

    keyframes("hueSpin") {
        to {
            filter = "hue-rotate(360deg)"
        }
    }

    // a {
    //    color: rgb(var(--primary));
    //    transition: color 0.3s ease;
    //}

    a {
        color = theme.primary
        transition += Transition("color", .3.s, Timing.ease)
    }

    // a:hover {
    //    color: rgb(var(--accentBg));
    //    animation: glow 10s infinite linear;
    //}

    "a:hover" {
        color = theme.accentBg
        animation += Animation("glow", 10.s, Timing.linear, iterationCount = IterationCount.infinite)
    }
}