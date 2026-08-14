package streetlight.web.ui

import kampfire.api.Markdown
import koala.Image
import koala.css.*
import koala.dom.*
import koala.html.Id
import koala.html.setPopoverTarget
import koala.model.storeOf
import streetlight.model.data.*
import streetlight.web.model.BlockEditor
import kotlin.uuid.Uuid

fun ViewScope.blockMenu(
    depth: Int,
    onSelection: (LayoutBlock) -> Unit,
) {
    val popoverId = Id(Uuid.random().toString())
    popoverCard(popoverId, mod = modify(), cardMod = modify(EditorBg, Padding0)) {
        val categoryField = storeOf<BlockCategory?>(null)
        flowBlock(categoryField, modify(Magic, Scale)) { category ->
            val options = when (category) {
                null -> {
                    val categories = getCategories(depth)
                    categories.map {
                        LabeledAction(it.name, { categoryField.set(it) }, modify(TextSmall, TextTransformUppercase))
                    }
                }

                else -> {
                    val options = getOptions(category, depth)
                    listOf(LabeledAction(
                        label = "← $category",
                        onClick = { categoryField.set(null) },
                        mod = modify(Bold, ZenBg, TextSmall, TextTransformUppercase)
                    )) + options.map { LabeledAction(it.label, { onSelection(it.item) }) }
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
    button(mod = modify(Padding1)) {
        textBlock("+ block", modify(Bold, TextTransformUppercase, TextSmall, EditorFg))
        setPopoverTarget(popoverId)
    }
}

fun getCategories(depth: Int) = buildList {
    add(BlockCategory.Basic)
    if (depth < 2) add(BlockCategory.Containers)
    add(BlockCategory.Content)
}

fun getOptions(category: BlockCategory, depth: Int) = buildList {
    when (category) {
        BlockCategory.Basic -> {
            add(LabeledItem("text", TextBlock("")))
            add(LabeledItem("image", ImageBlock(Image.Empty)))
            add(LabeledItem("rich text", RichTextBlock(Markdown.Empty)))
        }
        BlockCategory.Containers -> {
            if (depth == 0) add(LabeledItem("tabs", TabsBlock(listOf(TabContent("My Tab", emptyList())))))
            // containers for depth == 1 to be added
        }
        BlockCategory.Content -> {
            add(LabeledItem("header", HeaderBlock))
            add(LabeledItem("events", EventsBlock))
            add(LabeledItem("map", MapBlock))
        }
    }
}

enum class BlockCategory {
    Basic,
    Containers,
    Content,
}