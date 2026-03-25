package koala.dom

import koala.css.*
import kotlinx.coroutines.flow.Flow

fun RenderContext.messageBox(
    flow: Flow<UIMessage?>,
    modifiers: ModifierSet? = null,
    magic: Boolean = true
) {
    card(modify(ElementClass.messageBox, modifiers)) {
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
