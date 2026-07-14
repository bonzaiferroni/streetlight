package koala.html

import koala.css.*
import kotlinx.html.FlowContent

fun FlowContent.bulletsOf(mod: ModifierSet? = null, vararg content: String) {
    ulist(modify(mod, Gap0, ListStyleDisc, PaddingLeft3, ParagraphLineHeight), ListAxis.Column) {
        content.forEach { text ->
            listItem(text)
        }
    }
}

fun FlowContent.bulletsOf(vararg text: String) = bulletsOf(null, *text)

fun FlowContent.bulletsOf(content: List<String>) = bulletsOf(null, *content.toTypedArray())

fun FlowContent.bulletsOf(modifiers: ModifierSet, content: List<String>) {
    bulletsOf(modifiers, *content.toTypedArray())
}

fun FlowContent.bulletsOf(modifiers: ModifierSet? = null, vararg contents: FlowContent.() -> Unit) {
    ulist(modify(modifiers, Gap0, ListStyleDisc, PaddingLeft3, ParagraphLineHeight), ListAxis.Column) {
        contents.forEach { element ->
            listItem {
                element()
            }
        }
    }
}

fun FlowContent.bulletsOf(vararg content: FlowContent.() -> Unit) = bulletsOf(null, *content)