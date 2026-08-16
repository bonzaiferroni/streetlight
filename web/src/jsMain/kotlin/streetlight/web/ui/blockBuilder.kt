package streetlight.web.ui

import koala.Image
import koala.SvgFile
import koala.css.*
import koala.dom.*
import koala.model.storeOf
import koala.model.toggle
import kotlinx.html.FlowContent
import streetlight.model.data.*
import streetlight.web.layouts.renderImage
import streetlight.web.layouts.renderRichText
import streetlight.web.model.BlockEditor
import streetlight.web.model.BlockId
import streetlight.web.model.LayoutEditor

fun ViewScope.blockBuilder(model: LayoutEditor, blockId: BlockId) {
    val editor = model.getBlock(blockId)
    when (val block = editor.blockField.now) {
        HeaderBlock, EventsBlock, MapBlock -> blockBuilder(editor)
        is ImageBlock -> imageBuilder(editor)
        is RichTextBlock -> blockBuilder(editor) {
            renderRichText(block)
        }
        is TextBlock -> textBuilder(editor)
        is TabsBlock -> tabsBuilder(editor)
    }
}

fun ViewScope.blockBuilder(
    editor: BlockEditor,
    content: FlowContent.() -> Unit
) = column {
    editorRow(editor)
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

fun ViewScope.tabsBuilder(editor: BlockEditor) {
    // val tabsField = editor.blockField.narrow<LayoutBlock, TabsBlock>()
    column {
        editorRow(editor) {
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

fun ViewScope.textBuilder(editor: BlockEditor) {
    val textState = editor.mutableTapOf<TextBlock, String>({ it.text }) { copy(text = it) }
    val isEditingState = storeOf(textState.now.isEmpty())
    column {
        editorRow(editor, isEditingState)
        flowBlock(isEditingState) { isEditing ->
            if (isEditing) {
                textField(textState)
            } else {
                textBlock(textState.now.takeIf { it.isNotEmpty() } ?: "[Text content]")
            }
        }
    }
}

fun ViewScope.imageBuilder(editor: BlockEditor) {
    val imageState = editor.mutableTapOf<ImageBlock, Image?>({ it.image }) { copy(image = it)}
    column {
        editorRow(editor) {

        }
        imageDrop(imageState) { image ->
            image(image.url)
        }
    }
}