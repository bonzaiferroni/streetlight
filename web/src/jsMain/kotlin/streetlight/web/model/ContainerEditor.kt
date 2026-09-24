package streetlight.web.model

import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import streetlight.model.data.LayoutBlock

/** One container of a [LayoutEditor], holding an ordered list of blocks. */
class ContainerEditor(
    val containerId: ContainerId,
    name: String,
    blockIds: List<BlockId>,
    depth: Int,
    val model: LayoutEditor,
) {
    private val state = storeOf(ContainerState(name, blockIds, depth))
    val blockIdsField = state.mutableTapOf({ it.blockIds }) { copy(blockIds = it) }
    val childIds get() = state.now.blockIds
    val depth get() = state.now.depth
    val name get() = state.now.name
    val parent get() = model.getParentOrNull(containerId)
    // val parentId get() = parent.blockId

    /** Whether this container belongs directly to the block [blockId]. */
    fun isChildOf(blockId: BlockId) = model.getBlock(blockId).childIds.any { it == containerId }

    /** Whether this container sits anywhere inside the block [blockId]. */
    fun isDescendentOf(blockId: BlockId): Boolean {
        var container: ContainerEditor? = this
        while (container != null) {
            if (container.isChildOf(blockId)) return true
            container = container.parent?.parent
        }
        return false
    }

    fun rename(name: String) = state.set { copy(name = name) }

    /** Adds a new [block] at the end of this container. */
    fun createBlock(block: LayoutBlock) {
        model.addBlock(block, containerId, childIds.size)
    }

    /** Places the existing block [blockId] at [index]. */
    fun addBlock(blockId: BlockId, index: Int = childIds.size) {
        val newBlockIds = blockIdsField.now.toMutableList()
        newBlockIds.add(index, blockId)
        blockIdsField.set(newBlockIds)
    }

    fun removeBlock(blockId: BlockId) {
        val newBlockIds = blockIdsField.now.toMutableList()
        newBlockIds.remove(blockId)
        blockIdsField.set(newBlockIds)
    }
}

data class ContainerState(
    val name: String,
    val blockIds: List<BlockId>,
    val depth: Int,
)