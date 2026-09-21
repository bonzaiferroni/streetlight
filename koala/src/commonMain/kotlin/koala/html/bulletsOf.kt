package koala.html

import koala.modifier.*
import kotlinx.html.FlowContent

fun FlowContent.bulletsOf(mod: Modifier? = null, vararg content: String) {
    ulist(modify(mod, Gap(0), ListStyleDisc, PaddingLeft(3), ParagraphLineHeight), ListAxis.Column) {
        content.forEach { text ->
            listItem(text)
        }
    }
}

fun FlowContent.bulletsOf(vararg text: String) = bulletsOf(null, *text)

fun FlowContent.bulletsOf(content: List<String>) = bulletsOf(null, *content.toTypedArray())

fun FlowContent.bulletsOf(mod: Modifier, content: List<String>) {
    bulletsOf(mod, *content.toTypedArray())
}

fun FlowContent.bulletsOf(mod: Modifier? = null, vararg contents: FlowContent.() -> Unit) {
    ulist(modify(mod, Gap(0), ListStyleDisc, PaddingLeft(3), ParagraphLineHeight), ListAxis.Column) {
        contents.forEach { element ->
            listItem {
                element()
            }
        }
    }
}

fun FlowContent.bulletsOf(vararg content: FlowContent.() -> Unit) = bulletsOf(null, *content)