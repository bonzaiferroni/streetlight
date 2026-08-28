package koala.dom

import js.array.asList
import koala.dom.activeChunk
import koala.markdown.ContentBlock
import koala.model.EditorStyle
import web.dom.Node
import web.html.HTMLElement
import web.keyboard.KeyboardEvent
import web.window.window

fun HTMLElement.handleTableNavigation(event: KeyboardEvent): Boolean {
    val vertical = event.key == "ArrowUp" || event.key == "ArrowDown"
    val horizontal = event.key == "ArrowLeft" || event.key == "ArrowRight"
    if (!vertical && !horizontal) return false
    if (event.ctrlKey || event.altKey || event.metaKey || event.shiftKey) return false

    val block = activeChunk() ?: return false
    if (!block.isBlockType(ContentBlock.Table)) return false

    return when (vertical) {
        true -> moveTableVertical(block, event)
        else -> moveTableHorizontal(block, event)
    }
}

fun HTMLElement.isBlockType(type: ContentBlock) = getAttribute(EditorStyle.BlockType) == type

private fun HTMLElement.moveTableVertical(block: HTMLElement, event: KeyboardEvent): Boolean {
    val selection = window.getSelection() ?: return false
    if (selection.rangeCount == 0) return false

    val cell = selection.getRangeAt(0).startContainer.cellAncestor(this) ?: return false
    val row = cell.parentElement as? HTMLElement ?: return false
    val cellIndex = row.children.asList().indexOf(cell)

    val rows = row.parentElement?.children?.asList() ?: return false
    val targetIndex = when (event.key) {
        "ArrowUp" -> rows.indexOf(row) - 1
        else -> rows.indexOf(row) + 1
    }

    val targetRow = rows.getOrNull(targetIndex) as? HTMLElement
    if (targetRow == null) {
        val blocks = children.asList()
        val blockIndex = blocks.indexOf(block)
        val nextBlock = when (event.key) {
            "ArrowUp" -> blocks.getOrNull(blockIndex - 1)
            else -> blocks.getOrNull(blockIndex + 1)
        } as? HTMLElement ?: return false

        nextBlock.placeCaret(
            when (event.key) {
                "ArrowUp" -> nextBlock.textContent?.length ?: 0
                else -> 0
            }
        )
        event.preventDefault()
        return true
    }

    val target = targetRow.children.asList().getOrNull(cellIndex) as? HTMLElement ?: return false
    target.placeCaret(cell.caretOffset() ?: 0)
    event.preventDefault()
    return true
}

private fun HTMLElement.moveTableHorizontal(block: HTMLElement, event: KeyboardEvent): Boolean {
    val offset = block.caretOffset() ?: return false
    val target = when (event.key) {
        "ArrowRight" -> offset + 1
        else -> offset - 1
    }
    if (target < 0 || target > (block.textContent?.length ?: 0)) return false

    block.placeCaret(target)
    event.preventDefault()
    return true
}

private fun Node.cellAncestor(container: HTMLElement): HTMLElement? {
    var node: Node? = this
    while (node != null && node !== container) {
        val element = node as? HTMLElement
        if (element != null && (element.isModified(EditorStyle.TableCell) || element.isModified(EditorStyle.TableTail))) {
            return element
        }
        node = node.parentNode
    }
    return null
}