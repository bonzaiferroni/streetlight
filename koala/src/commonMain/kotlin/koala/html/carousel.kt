package koala.html

import koala.css.Css
import koala.css.ModifierSet
import koala.css.addModifiers
import kotlinx.html.FlowContent
import kotlinx.html.DIV
import kotlinx.html.div

fun FlowContent.carousel(modifiers: ModifierSet? = null, content: DIV.() -> Unit) {
    div {
        addModifiers(CarouselKey.Class, modifiers)
        row(block = content)
    }
}

object CarouselKey {
    val Class = Css("carousel")
}

// language="CSS"
val CarouselCss get() = """
.carousel {
    width: 100%;
    overflow-x: auto;
    overflow-y: visible;
    scroll-snap-type: x mandatory;
    -webkit-overflow-scrolling: touch;
    scrollbar-width: none;
}

.carousel::-webkit-scrollbar {
    display: none;
}

.carousel > .row {
    display: flex;
    flex-wrap: nowrap;
    width: max-content;
}

.carousel > .row > * {
    scroll-snap-align: start;
}
"""