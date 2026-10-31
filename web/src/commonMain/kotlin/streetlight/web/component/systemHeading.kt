package streetlight.web.component

import koala.component.filigree
import koala.component.heading4
import koala.modifier.*
import kotlinx.html.FlowContent
import kotlinx.html.H4

/** A heading of [text] in the system style, uppercase in a filigree, with [block] applied to the heading. */
fun FlowContent.systemHeading(
    text: String,
    block: H4.() -> Unit = {},
) = filigree(ruleMod = MaxWidth(32)) {
    heading4(text, modify(SystemFg, TextUppercase, MoonShadowText), block)
}
