package koala.css

import kotlinx.css.*
import kotlinx.css.properties.Timing
import kotlinx.css.properties.Transition
import kotlinx.css.properties.s

object ElementClass {
    val lottie = Css("lottie")
    val icon = Css("icon")
    val logo = Css("logo")
    val action = Css("action")
    val button = Css("btn")
    val flowBlock = Css("flow-block")
    val itemsBlock = Css("items-block")
    val textLabel = Css("text-label")
    val blockLabel = Css("block-label")
    val thumbImage = Css("thumb-image")
    val shellBox = Css("shell-box")
    val headerImage = Css("header-image")
    val wireBlock = Css("wire-block")
}

fun CssBuilder.elementStyles(theme: KoalaTheme) {
    rule(ElementClass.lottie) {
        child("svg") {
//            display = Display.block
//            maxHeight = 100.pct
        }
    }

    rule(ElementClass.icon) {
        display = Display.inlineBlock
        backgroundColor = Color.currentColor
        put("aspect-ratio", "1 / 1")
        put("mask-image", "var(--mask-src)")
        put("-webkit-mask-image", "var(--mask-src)")
        put("mask-size", "contain")
        put("-webkit-mask-size", "contain")
        put("mask-repeat", "no-repeat")
        put("-webkit-mask-repeat", "no-repeat")
        put("mask-position", "center")
        put("-webkit-mask-position", "center")
    }

    rule(ElementClass.action) {
        transition += Transition("opacity", 0.35.s, Timing.ease)
        cursor = Cursor.pointer

        hover {
            opacity = 1
        }
    }

    rule(ElementClass.textLabel) {
        fontSize = .8.rem
        opacity = .5
    }
}