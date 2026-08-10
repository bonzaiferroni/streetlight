package streetlight.web.ui

import koala.SvgFile
import koala.css.*
import koala.dom.*
import koala.dom.button
import koala.dom.row
import koala.html.Id
import koala.html.setPopoverTarget
import koala.model.MutableField
import kotlinx.html.BUTTON
import streetlight.model.data.LocationConfig
import streetlight.model.data.LocationContent
import streetlight.model.data.TextBlock
import streetlight.web.model.BlockEditor
import streetlight.web.model.LayoutEditor
import kotlin.uuid.Uuid

fun ViewScope.layoutBuilder(model: LayoutEditor, content: LocationContent) {
    containerBuilder(model, model.mainContainerId, content)
}

fun ViewScope.containerBuilder(model: LayoutEditor, containerId: Uuid, content: LocationContent) {
    val editor = model.getContainer(containerId)
    flowBlock(editor.blockIdsField) { blockIds ->
        column {
            blockIds.forEach { blockId ->
                blockBuilder(model, blockId, content)
            }
            blockZone(model, containerId, blockIds.size)
        }
    }
}

fun ViewScope.blockZone(model: LayoutEditor, containerId: Uuid, index: Int) {
    button(SvgFile.Plus, {
        model.addBlock(TextBlock("My Text"), containerId, index)
    })
}

fun ViewScope.menuEditRow(name: String, editor: BlockEditor, menuContent: ViewScope.() -> Unit) {
    val popoverId = Id(Uuid.random().toString())
    popoverCard(popoverId) {
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
    row(modify(JustifyContentSpaceBetween, AlignItemsCenter)) {
        button(SvgFile.Plus, { editor.addBlockAbove(TextBlock("My Text")) })
        button("edit $name", onClick, modify(Zen), block = configButton)
    }
}