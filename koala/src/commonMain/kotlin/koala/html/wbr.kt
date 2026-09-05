package koala.html

import kotlinx.html.FlowOrPhrasingContent
import kotlinx.html.HTMLTag
import kotlinx.html.visit

fun FlowOrPhrasingContent.wbr() = HTMLTag("wbr", consumer, emptyMap(), inlineTag = true, emptyTag = true).visit {}