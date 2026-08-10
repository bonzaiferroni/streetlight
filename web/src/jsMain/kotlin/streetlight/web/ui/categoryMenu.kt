package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.Id
import koala.html.setPopoverTarget
import koala.model.storeOf
import streetlight.model.data.LayoutBlock
import kotlin.uuid.Uuid

fun ViewScope.categoryMenu(
    name: String,
    categories: Map<String, List<LabeledItem<out LayoutBlock>>>,
    onSelection: (LayoutBlock) -> Unit,
) {
    val popoverId = Id(Uuid.random().toString())
    popoverCard(popoverId, mod = modify(EditorBg, Padding0)) {
        val categoryField = storeOf<String?>(null)
        flowBlock(categoryField, modify(Magic, Blur, Scale)) { category ->
            val options = when (category) {
                null -> {
                    categories.map {
                        LabeledAction(it.key, { categoryField.set(it.key) }, modify(TextSmall, TextTransformUppercase))
                    }
                }

                else -> {
                    listOf(LabeledAction(
                        label = "← $category",
                        onClick = { categoryField.set(null) },
                        mod = modify(Bold, ZenBg, TextSmall, TextTransformUppercase)
                    )) + categories.getValue(category).map { LabeledAction(it.label, { onSelection(it.item) }) }
                }
            }
            column(modify(Gap0)) {
                options.forEach { option ->
                    button(option.onClick, mod = modify(option.mod, Padding1, MinWidth16)) {
                        textBlock(option.label, modify(TextAlignCenter, Width100P))
                    }
                }
            }
        }
    }
    button(name, mod = modify(Zen, EditorBg)) {
        setPopoverTarget(popoverId)
    }
}