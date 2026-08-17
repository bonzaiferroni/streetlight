package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.dom.button
import koala.dom.row
import koala.html.Id
import koala.html.heading3
import koala.html.hr
import koala.html.setPopoverTarget
import koala.html.textBlock
import koala.model.MutableTap
import koala.model.toggle
import kotlinx.html.BUTTON
import streetlight.web.model.BlockEditor
import streetlight.web.model.BlockId
import streetlight.web.model.ContainerEditor
import streetlight.web.model.ContainerId
import streetlight.web.model.LayoutEditor
import kotlin.uuid.Uuid

fun ViewScope.layoutBuilder(model: LayoutEditor) {
    column(BodyStyle.Column) {
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
        column(modify(if (editor.depth > 0) EditorStyle.Container else null, BorderRadius1)) {
            blockIds.forEach { blockId ->
                blockBuilder(model, blockId)
            }
            lastEditorRow(editor)
        }
    }
}

fun ViewScope.editorRow(
    editor: BlockEditor,
    isEditingState: MutableTap<Boolean>? = null,
    menuContent: (ViewScope.() -> Unit)? = null,
) {
    val popoverId = menuContent?.let {
        Id(Uuid.random().toString()).also {
            popoverCard(it, cardMod = modify(EditorBg)) {
                menuContent()
            }
        }
    }
    flowBlock(editor.model.movingBlockIdState, modify(Magic, Scale, Height5)) { movingBlockId ->
        when (movingBlockId) {
            null -> {
                row(modify(AlignItemsCenter)) {
                    row(modify(AlignItemsCenter, Flex1)) {
                        blockMenu(editor.depth ?: error("depth is null")) { editor.addBlockAbove(it) }
                        editorTextButton("remove", { editor.removeFromLayout() })
                        hr(modify(Flex1))
                    }
                    when (popoverId) {
                        null -> textBlock(editor.label, modify(BodyStyle.LabelHeading, EditorFg, Bold))
                        else -> button("edit ${editor.label}", mod = modify(Editor, JustifySelfCenter)) {
                            setPopoverTarget(popoverId)
                        }
                    }

                    row(modify(AlignItemsCenter, Flex1)) {
                        hr(modify(Flex1))
                        isEditingState?.let { state ->
                            flowBlock(state) { isEditing ->
                                editorTextButton(if (isEditing) "done" else "edit", { state.toggle() })
                            }
                        }
                        editorTextButton("move", { editor.model.startMove(editor.blockId) })
                    }
                }
            }
            else -> {
                if (editor.isNextSiblingOf(movingBlockId) || editor.isChildOf(movingBlockId)) return@flowBlock
                val isOriginalLocation = editor.model.movingBlockIdState.now == editor.blockId
                val label = if (isOriginalLocation) "cancel move" else "move here"
                val mod = if (isOriginalLocation) Secondary else Editor
                button(label, {
                    if (isOriginalLocation) editor.model.cancelMove()
                    else editor.model.finishMove(editor.blockId)
                }, modify(mod, Width100P))
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
                }, EditorStyle.TextButton) {
                    textBlock("restore")
                }
            }
        }
    }
}

fun ViewScope.editorTextButton(
    name: String,
    onClick: (() -> Unit)? = null,
    config: BUTTON.() -> Unit = { }
) = button(onClick, modify(Padding1)) {
    config()
    textBlock(name, EditorStyle.TextButton)
}

object EditorStyle {
    val TextButton = modify(TextUppercase, TextSmall, Bold, EditorFg)
    val Container = modify(ZenBg, MoonShadow, Padding1)
}