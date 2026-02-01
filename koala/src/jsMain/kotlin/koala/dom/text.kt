package koala.dom

import kotlinx.html.js.p

fun DOMContext.paragraph(text: String) = p {
    +text
}