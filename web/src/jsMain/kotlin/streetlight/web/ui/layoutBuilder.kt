package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.dom.button
import koala.dom.row
import koala.html.Id
import koala.html.box
import koala.html.heading3
import koala.html.hr
import koala.html.setPopoverTarget
import koala.html.textBlock
import kotlinx.html.BUTTON
import kotlinx.html.FlowContent
import streetlight.model.data.BlockType
import streetlight.web.model.BlockEditor
import streetlight.web.model.BlockId
import streetlight.web.model.ContainerEditor
import streetlight.web.model.ContainerId
import streetlight.web.model.LayoutEditor
import kotlin.uuid.Uuid

fun ViewScope.layoutBuilder(model: LayoutEditor) {
    column(BodyStyle.column) {
        containerBuilder(model, model.mainContainerId)
        // td: footer designer
        flowBlock(model.removedBlockIdsState) { removedBlockIds ->
            if (removedBlockIds.isEmpty()) return@flowBlock
            removedBlockList(model, removedBlockIds)
        }
    }
}

fun ViewScope.containerBuilder(model: LayoutEditor, containerId: ContainerId) {
    val editor = model.getContainer(containerId)
    flowBlock(editor.blockIdsField) { blockIds ->
        column(modify(if (editor.depth > 0) modify(ZenBg, MoonShadow, Padding1) else null, BorderRadius1)) {
            blockIds.forEach { blockId ->
                blockBuilder(model, blockId)
            }
            lastEditorRow(editor)
        }
    }
}

fun ViewScope.menuEditRow(editor: BlockEditor, menuContent: ViewScope.() -> Unit) {
    val popoverId = Id(Uuid.random().toString())
    popoverCard(popoverId, cardMod = modify(EditorBg)) {
        menuContent()
    }
    editorRow(editor, null) {
        setPopoverTarget(popoverId)
    }
}

fun ViewScope.editorRow(
    editor: BlockEditor,
    onClick: (() -> Unit)?,
    configButton: BUTTON.() -> Unit = { }
) {
    flowBlock(editor.model.movingBlockIdState, modify(Magic, Scale, Height5)) { movingBlockId ->
        when (movingBlockId) {
            null -> {
                row(modify(AlignItemsCenter, PaddingX1)) {
                    row(modify(AlignItemsCenter, Flex1)) {
                        blockMenu(editor.depth ?: error("depth is null")) { editor.addBlockAbove(it) }
                        hr(modify(Flex1))
                    }
                    button("edit ${editor.label}", onClick, modify(Editor, JustifySelfCenter), block = configButton)
                    row(modify(AlignItemsCenter, Flex1)) {
                        hr(modify(Flex1))
                        button({
                            editor.model.startMove(editor.blockId)
                        }, modify(Padding1)) {
                            textBlock("move", LayoutBuilder.TextButtonMod)
                        }
                    }
                }
            }
            else -> {
                if (editor.isNextSiblingOf(movingBlockId) || editor.isChildOf(movingBlockId)) return@flowBlock
                val isOriginalLocation = editor.model.movingBlockIdState.now == editor.blockId
                val label = if (isOriginalLocation) "cancel move" else "move here"
                button(label, {
                    if (isOriginalLocation) editor.model.cancelMove()
                    else editor.model.finishMove(editor.blockId)
                }, modify(Editor, OutlineDashed2Px, Width100P))
            }
        }
    }
}

fun ViewScope.lastEditorRow(
    editor: ContainerEditor
) {
    flowBlock(editor.model.movingBlockIdState, modify(Magic, Scale, Height5)) { movingBlockId ->
        when (movingBlockId) {
            null -> {
                row(modify(AlignItemsCenter)) {
                    blockMenu(editor.depth, { editor.createBlock(it)} )
                }
            }
            else -> {
                val blockEditor = editor.model.getBlock(movingBlockId)
                val index = blockEditor.index
                val isNextPosition = blockEditor.parentId == editor.containerId
                        && index != null && index + 1 == editor.childIds.size
                if (editor.isChildOf(movingBlockId) || isNextPosition) return@flowBlock
                button("move here", {
                    editor.model.finishMoveToContainer(editor.containerId)
                }, modify(Zen, EditorBg, OutlineDashed2Px, Width100P))
            }
        }
    }
}

fun ViewScope.removedBlockList(model: LayoutEditor, blockIds: List<BlockId>) {
    column {
        filigree { heading3("Removed Blocks") }
        blockIds.forEach { blockId ->
            val blockEditor = model.getBlock(blockId)
            val blockName = blockEditor.block::class.simpleName ?: "[block]"
            row(modify(JustifyContentSpaceBetween)) {
                textBlock(blockName)
                button({
                    model.startMove(blockId)
                }, LayoutBuilder.TextButtonMod) {
                    textBlock("restore")
                }
            }
        }
    }
}

object LayoutBuilder {
    val TextButtonMod = modify(TextUppercase, TextSmall, Bold, EditorFg)
}