package koala.html

import kotlinx.html.FlowOrPhrasingContent
import kotlinx.html.HTMLTag
import kotlinx.html.visit

/** A point where the browser may break a line. */
fun FlowOrPhrasingContent.wbr() = HTMLTag("wbr", consumer, emptyMap(), inlineTag = true, emptyTag = true).visit {}