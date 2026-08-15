package streetlight.web.ui

import initElement
import koala.SvgFile
import koala.css.*
import koala.dom.*
import koala.model.storeOf
import koala.model.toggle
import kotlinx.html.FlowContent
import org.w3c.dom.HTMLElement
import streetlight.model.data.*
import streetlight.web.layouts.renderEvents
import streetlight.web.layouts.renderHeader
import streetlight.web.layouts.renderImage
import streetlight.web.layouts.renderMap
import streetlight.web.layouts.renderRichText
import streetlight.web.model.BlockEditor
import streetlight.web.model.BlockId
import streetlight.web.model.LayoutEditor

fun ViewScope.blockBuilder(model: LayoutEditor, blockId: BlockId) {
    val editor = model.getBlock(blockId)
    when (val block = editor.blockField.now) {
        HeaderBlock, EventsBlock, MapBlock -> blockBuilder(editor)
        is ImageBlock -> blockBuilder(editor) {
            renderImage(block)
        }
        is RichTextBlock -> blockBuilder(editor) {
            renderRichText(block)
        }
        is TextBlock -> textBuilder(editor)
        is TabsBlock -> tabsBuilder(editor)
    }
}

fun ViewScope.blockBuilder(
    editor: BlockEditor,
    onClick: (() -> Unit)?,
    content: ViewScope.() -> Unit
) {
    column {
        editorRow(editor, onClick)
        content()
    }
}

fun ViewScope.blockBuilder(
    editor: BlockEditor,
    content: FlowContent.() -> Unit
) = column {
    menuEditRow(editor) {
        button({
            editor.removeFromLayout()
        }) {
            textBlock("remove ${editor.label}")
        }
    }
    content()
}

fun ViewScope.blockBuilder(
    editor: BlockEditor,
) {
    val blockType = editor.block.blockType
    blockBuilder(editor) {
        buildContentBlock(blockType)
    }
}

fun ViewScope.buildContentBlock(type: BlockType) {
    box(modify(Outline, BorderRadius1, ZenBg, Height48)) {
        textBlock("${type.label} content", modify(TextUppercase, TextSmall, OpacityHigh, PlaceSelfCenter))
    }
}

fun ViewScope.textBuilder(editor: BlockEditor) {
    val textField = editor.mutableFieldOf<TextBlock, String>({ it.text }) { copy(text = it) }
    val isEditingField = storeOf(textField.now.isEmpty())
    blockBuilder(editor, isEditingField::toggle) {
        flowBlock(isEditingField) { isEditing ->
            if (isEditing) {
                textField(textField)
            } else {
                textBlock(textField.now.takeIf { it.isNotEmpty() } ?: "[Text content]")
            }
        }
    }
}

fun ViewScope.tabsBuilder(editor: BlockEditor) {
    // val tabsField = editor.blockField.narrow<LayoutBlock, TabsBlock>()
    column {
        menuEditRow(editor) {
            flowBlock(editor.refreshField) {
                column {
                    editor.childIds.forEach { containerId ->
                        val tabName = editor.model.getContainer(containerId).name
                        val isEditingField = storeOf(false)
                        flowBlock(isEditingField, modify(Height5)) { isEditing ->
                            if (isEditing) {
                                val tabNameField = storeOf(tabName)
                                textField(tabNameField, onEnter = {
                                    editor.renameContainer(containerId, tabNameField.now)
                                })
                            } else {
                                row(modify(AlignItemsCenter, Height5)) {
                                    textBlock(tabName, modify(Flex1))
                                    button(SvgFile.Edit, { isEditingField.toggle() })
                                    button(SvgFile.Minus, {
                                        editor.removeContainer(containerId)
                                    })
                                }
                            }
                        }
                    }
                    button(SvgFile.Plus, {
                        editor.addContainer(TabContent("new tab", emptyList()))
                    })
                }
            }
        }

        flowBlock(editor.refreshField) {
            tabs {
                editor.childIds.forEach { containerId ->
                    val container = editor.model.getContainer(containerId)
                    tab(container.name) {
                        containerBuilder(editor.model, containerId)
                    }
                }
            }
        }
    }
}