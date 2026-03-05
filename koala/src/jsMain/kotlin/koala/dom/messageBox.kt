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
            flowBlock(flow, modify(Flex1), magic = magic) { message ->
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
