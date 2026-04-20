package koala.dom

import koala.css.*
import koala.html.MessageBoxKey
import kotlinx.coroutines.flow.Flow

fun RenderContext.messageBox(
    flow: Flow<UIMessage?>,
    modifiers: ModifierSet? = null,
    magic: Boolean = true
) {
    card(modify(modifiers, MessageBoxKey.Class, MoonShadow)) {
        row(modify(AlignItemsStart)) {
//            image(SiteImage.placeholderThumb, modify(Width4))
            val modifiers = modify(Flex1).let {
                if (magic) it + Magic else it
            }
            flowBlock(flow, modifiers) { message ->
                column {
                    val paragraphs = message?.text?.split("\n\n")
                    paragraphs?.forEach { text ->
                        textBlock(text)
                    }
                }
            }
        }
    }
}