package streetlight.web.ui

import koala.Image
import koala.SvgFile
import koala.css.*
import koala.dom.*
import koala.model.mutableTapOf
import koala.model.refine
import koala.model.storeOf
import koala.model.toggle
import kotlinx.html.FlowContent
import streetlight.model.data.*
import streetlight.web.layouts.renderRichText
import streetlight.web.model.BlockEditor
import streetlight.web.model.BlockId
import streetlight.web.model.LayoutEditor

fun ViewScope.blockBuilder(model: LayoutEditor, blockId: BlockId) {
    val editor = model.getBlock(blockId)
    when (val block = editor.blockState.now) {
        HeaderBlock, EventsBlock, MapBlock -> blockBuilder(editor)
        is ImageBlock -> imageBuilder(editor)
        is RichTextBlock -> blockBuilder(editor) {
            renderRichText(block)
        }
        is TextBlock -> textBuilder(editor)
        is TabsBlock -> tabsBuilder(editor)
        is ColumnBlock -> columnBuilder(editor)
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
            flowBlock(editor.refreshState) {
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

        flowBlock(editor.refreshState) {
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
    val blockState = editor.blockState.mutableTapOf({ it as TextBlock }) { it }
    val textState = blockState.mutableTapOf({ it.text }) { copy(text = it) }
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
    val blockState = editor.blockState.mutableTapOf({ it as ImageBlock }) { it }
    val imageState = blockState.mutableTapOf({ it.image }) { copy(image = it) }
    val shapeState = blockState.mutableTapOf({ it.frame }) { copy(frame = it) }
    val fitState = blockState.mutableTapOf({ it.fit }) { copy(fit = it) }
    column {
        editorRow(editor) {
            formColumn {
                formRow {
                    formSection("Shape / Fit") {
                        dropMenuNullable(shapeState)
                        dropMenuNullable(fitState)
                    }
                }
            }
        }
        imageDrop(imageState) { image ->
            flowBlock(blockState, modify(Magic, Scale)) { block ->
                val shapeMod = when (block.frame) {
                    ImageFrame.Circle -> CircleShape
                    ImageFrame.Ellipse -> BorderRadius50P
                    ImageFrame.Pill -> BorderRadiusPill
                    ImageFrame.Chopped -> Chopped
                    null -> null
                }
                val fitMod = when (block.fit) {
                    ObjectFit.Fill -> ObjectFitFill
                    ObjectFit.Contain -> ObjectFitContain
                    ObjectFit.Cover -> ObjectFitCover
                    ObjectFit.ScaleDown -> ObjectFitScaleDown
                    null -> null
                }
                image(image.url, modify(shapeMod, fitMod))
            }
        }
    }
}

fun ViewScope.columnBuilder(editor: BlockEditor) {
    // val blockState = editor.blockState.mutableTapOf({ it as ColumnBlock }) { it }
    val containerId = editor.childIds.first()
    val container = editor.model.getContainer(containerId)
    flowBlock(container.blockIdsField) { blockIds ->
        row(modify(EditorStyle.Container, BodyStyle.FlexGrid2)) {
            blockIds.forEach { blockId ->
                blockBuilder(editor.model, blockId)
            }
            lastEditorRow(container)
        }
    }
}