package streetlight.web.model

import koala.model.mutableTapOf
import koala.model.storeOf
import streetlight.model.data.LayoutBlock

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

    fun isChildOf(blockId: BlockId) = model.getBlock(blockId).childIds.any { it == containerId }

    fun isDescendentOf(blockId: BlockId): Boolean {
        var container: ContainerEditor? = this
        while (container != null) {
            if (container.isChildOf(blockId)) return true
            container = parent?.parent
        }
        return false
    }

    fun rename(name: String) = state.set { copy(name = name) }

    fun createBlock(block: LayoutBlock) {
        model.addBlock(block, containerId, childIds.size)
    }

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