package koala.html

import koala.modifier.*
import kotlinx.html.FlowContent
import kotlinx.html.DIV
import kotlinx.html.div

fun FlowContent.carousel(mod: Modifier? = null, content: DIV.() -> Unit) {
    div {
        addModifiers(CarouselKey.Class, mod)
        row(block = content)
    }
}

object CarouselKey {
    val Class = Class("carousel")
}

// language="CSS"
val CarouselCss get() = with(CarouselKey) { """
$Class {
    width: 100%;
    overflow-x: auto;
    overflow-y: visible;
    scroll-snap-type: x mandatory;
    -webkit-overflow-scrolling: touch;
    scrollbar-width: none;
}

$Class::-webkit-scrollbar {
    display: none;
}

$Class > $FlexRow {
    display: flex;
    flex-wrap: nowrap;
    width: max-content;
}

$Class > $FlexRow > * {
    scroll-snap-align: start;
}
""" }