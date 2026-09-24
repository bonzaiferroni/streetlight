package streetlight.web.ui

import kampfire.api.Markdown
import koala.SvgFile
import koala.modifier.*
import koala.dom.*
import koala.html.Id
import koala.modifier.setPopoverTarget
import koala.markdown.HeadingLevel
import kampfire.model.storeOf
import streetlight.model.data.*
import kotlin.uuid.Uuid

/**
 * A button that opens the menu of blocks to add, by category, calling [onSelection] with the chosen block.
 *
 * [name] labels the container added to; without it the button adds above a block. Containers are offered only
 * above [depth] 2.
 */
fun ViewScope.blockMenu(
    name: String?,
    depth: Int,
    onSelection: (LayoutBlock) -> Unit,
) {
    val popoverId = Id(Uuid.random().toString())
    popover(popoverId, SystemBg) {
        val categoryField = storeOf<BlockCategory?>(null)
        flowBlock(categoryField, modify(Magic, Scale)) { category ->
            val options = when (category) {
                null -> {
                    val categories = getCategories(depth)
                    categories.map {
                        MenuAction(it.name, modify(TextSmall, TextUppercase)) { categoryField.set(it) }
                    }
                }

                else -> {
                    val options = getOptions(category, depth)
                    listOf(
                        MenuAction("← $category", modify(Bold, ZenBg, TextSmall, TextUppercase)) { categoryField.set(null) }
                    ) + options.map { MenuAction(it.label) { onSelection(it.value) } }
                }
            }
            column(Gap0) {
                options.forEach { option ->
                    popoverOption(option)
                }
            }
        }
    }
    row(modify(AlignItemsCenter, LargeIconHeight)) {
        name?.let {
            textBlock("add to $name", modify(TextSmall, TextUppercase, OpacityHalf))
        }
        editorIconButton(if (name == null) SvgFile.PlusAbove else SvgFile.Plus) {
            setPopoverTarget(popoverId)
        }
    }
}

fun getCategories(depth: Int) = buildList {
    add(BlockCategory.Basic)
    if (depth < 2) add(BlockCategory.Containers)
    add(BlockCategory.Content)
}

/** The blocks of [category] that can be added at [depth]. */
fun getOptions(category: BlockCategory, depth: Int) = buildList {
    when (category) {
        BlockCategory.Basic -> {
            add(MenuValue("text", TextBlock("")))
            add(MenuValue("heading", HeadingBlock("", HeadingLevel.H3, true)))
            add(MenuValue("image", ImageBlock(null)))
            add(MenuValue("rich text", RichTextBlock(Markdown.Empty, null)))
            add(MenuValue("gallery", GalleryBlock(emptyList(), 2)))
        }
        BlockCategory.Containers -> {
            if (depth == 0) add(MenuValue("tabs", TabsBlock(listOf(TabContent("My Tab", emptyList())))))
            add(MenuValue("column", ColumnsBlock(emptyList())))
            // containers for depth == 1 to be added
        }
        BlockCategory.Content -> {
            add(MenuValue("header", HeaderBlock))
            add(MenuValue("events", EventsBlock))
            add(MenuValue("map", MapBlock))
        }
    }
}

enum class BlockCategory {
    Basic,
    Containers,
    Content,
}