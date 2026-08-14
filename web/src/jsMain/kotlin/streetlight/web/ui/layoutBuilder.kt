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
import streetlight.model.data.LocationContent
import streetlight.web.model.BlockEditor
import streetlight.web.model.ContainerEditor
import streetlight.web.model.ContainerId
import streetlight.web.model.LayoutEditor
import kotlin.uuid.Uuid

fun ViewScope.layoutBuilder(model: LayoutEditor, content: LocationContent) {
    containerBuilder(model, model.mainContainerId, content)
}

fun ViewScope.containerBuilder(model: LayoutEditor, containerId: ContainerId, content: LocationContent) {
    val editor = model.getContainer(containerId)
    flowBlock(editor.blockIdsField) { blockIds ->
        column(modify(if (editor.depth > 0) modify(ZenBg, MoonShadow, Padding1) else null, BorderRadius1)) {
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
    flowBlock(editor.model.movingBlockField, modify(Magic, Scale, Height5)) { movingBlockId ->
        when (movingBlockId) {
            null -> {
                box {
                    row(modify(AlignItemsCenter, PaddingX1)) {
                        blockMenu(editor.depth, { editor.addBlockAbove(it)} )
                        spacer(modify(Flex1))
                        button({
                            editor.model.startMove(editor.blockId)
                        }, modify(Padding1)) {
                            textBlock("move", modify(TextTransformUppercase, TextSmall, Bold, EditorFg))
                        }
                    }
                    button("edit $name", onClick, modify(Zen, EditorBg, JustifySelfCenter), block = configButton)
                }
            }
            else -> {
                if (editor.isNextSiblingOf(movingBlockId) || editor.isChildOf(movingBlockId)) return@flowBlock
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
    flowBlock(editor.model.movingBlockField, modify(Magic, Scale, Height5)) { movingBlockId ->
        when (movingBlockId) {
            null -> {
                row(modify(AlignItemsCenter)) {
                    blockMenu(editor.depth, { editor.createBlock(it)} )
                }
            }
            else -> {
                val blockEditor = editor.model.getBlock(movingBlockId)
                val isNextPosition = blockEditor.parentId == editor.containerId && blockEditor.index + 1 == editor.childIds.size
                if (editor.isChildOf(movingBlockId) || isNextPosition) return@flowBlock
                button("move here", {
                    editor.model.finishMoveToContainer(editor.containerId)
                }, modify(Zen, EditorBg, OutlineDashed2Px, Width100P))
            }
        }
    }
}
