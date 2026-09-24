package streetlight.web.model

import streetlight.model.data.*

/**
 * Rebuilds [block] with the contents of its edited [containers].
 *
 * Returns `null` for a tabs block without tabs; a columns block without containers is returned unchanged.
 */
fun buildBlock(block: LayoutBlock, containers: List<ContainerDefinition>?): LayoutBlock? {
    return when (block) {
        is TabsBlock -> {
            val tabContents = containers?.takeIf { it.isNotEmpty() }?.map {
                TabContent(it.name, it.blocks)
            } ?: return null
            TabsBlock(tabContents)
        }
        is ColumnsBlock -> {
            block.copy(blocks = containers?.firstOrNull()?.blocks ?: return block)
        }
        else -> block
    }
}