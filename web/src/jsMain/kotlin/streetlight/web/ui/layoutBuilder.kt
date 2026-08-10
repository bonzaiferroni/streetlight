package streetlight.web.ui

import kampfire.api.Markdown
import koala.Image
import koala.css.*
import koala.dom.*
import koala.dom.button
import koala.dom.row
import koala.html.Id
import koala.html.setPopoverTarget
import koala.html.spacer
import kotlinx.html.BUTTON
import streetlight.model.data.EventsBlock
import streetlight.model.data.HeaderBlock
import streetlight.model.data.ImageBlock
import streetlight.model.data.LocationContent
import streetlight.model.data.MapBlock
import streetlight.model.data.RichTextBlock
import streetlight.model.data.TabContent
import streetlight.model.data.TabsBlock
import streetlight.model.data.TextBlock
import streetlight.web.model.BlockEditor
import streetlight.web.model.ContainerEditor
import streetlight.web.model.LayoutEditor
import kotlin.uuid.Uuid

fun ViewScope.layoutBuilder(model: LayoutEditor, content: LocationContent) {
    containerBuilder(model, model.mainContainerId, content)
}

fun ViewScope.containerBuilder(model: LayoutEditor, containerId: Uuid, content: LocationContent) {
    val editor = model.getContainer(containerId)
    flowBlock(editor.blockIdsField) { blockIds ->
        column(modify(BorderRadius1, if (editor.depth > 0) ZenBg else null)) {
            blockIds.forEach { blockId ->
                blockBuilder(model, blockId, content)
            }
            lastEditorRow(editor)
        }
    }
}

fun ViewScope.menuEditRow(name: String, editor: BlockEditor, menuContent: ViewScope.() -> Unit) {
    val popoverId = Id(Uuid.random().toString())
    popoverCard(popoverId, cardMod = modify(EditorBg)) {
        menuContent()
    }
    editorRow(name, editor, null) {
        setPopoverTarget(popoverId)
    }
}

fun ViewScope.editorRow(
    name: String,
    editor: BlockEditor,
    onClick: (() -> Unit)?,
    configButton: BUTTON.() -> Unit = { }
) {
    flowBlock(editor.model.movingBlockField, modify(Magic, Scale)) { movingBlockId ->
        when (movingBlockId) {
            null -> {
                row(modify(AlignItemsCenter)) {
                    blockMenu(BlockMenu.categories, { editor.addBlockAbove(it)} )
                    spacer(modify(Flex1))
                    button({
                        editor.model.startMove(editor.blockId)
                    }) {
                        textBlock("move", modify(TextTransformUppercase, TextSmall, Bold, EditorFg))
                    }
                    button("edit $name", onClick, modify(Zen, EditorBg), block = configButton)
                }
            }
            else -> {
                val isOriginalLocation = editor.model.movingBlockField.now == editor.blockId
                val label = if (isOriginalLocation) "cancel move" else "move here"
                button(label, {
                    if (isOriginalLocation) editor.model.cancelMove()
                    else editor.model.finishMove(editor.blockId)
                }, modify(Zen, EditorBg, OutlineDashed2Px, Width100P))
            }
        }
    }
}

fun ViewScope.lastEditorRow(
    editor: ContainerEditor
) {
    flowBlock(editor.model.movingBlockField, modify(Magic, Scale)) { movingBlockId ->
        when (movingBlockId) {
            null -> {
                row(modify(AlignItemsCenter)) {
                    blockMenu(BlockMenu.categories, { editor.createBlock(it)} )
                }
            }
            else -> {
                button("move here", {
                    editor.model.finishMoveToContainer(editor.containerId)
                }, modify(Zen, EditorBg, OutlineDashed2Px, Width100P))
            }
        }
    }
}

object BlockMenu {
    val categories = mapOf(
        "basic" to listOf(
            LabeledItem("text", TextBlock("")),
            LabeledItem("image", ImageBlock(Image.Empty)),
            LabeledItem("rich text", RichTextBlock(Markdown.Empty)),
        ),
        "containers" to listOf(
            LabeledItem("tabs", TabsBlock(listOf(TabContent("My Tab", emptyList()))))
        ),
        "content" to listOf(
            LabeledItem("header", HeaderBlock),
            LabeledItem("events", EventsBlock),
            LabeledItem("map", MapBlock),
        )
    )
}