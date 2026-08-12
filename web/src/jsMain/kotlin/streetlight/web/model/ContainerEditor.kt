package streetlight.web.model

import koala.model.mutableTapOf
import koala.model.storeOf
import streetlight.model.data.LayoutBlock
import kotlin.uuid.Uuid

class ContainerEditor(
    val containerId: Uuid,
    name: String,
    blockIds: List<Uuid>,
    depth: Int,
    val model: LayoutEditor,
) {
    private val state = storeOf(ContainerState(name, blockIds, depth))
    val blockIdsField = state.mutableTapOf({ it.blockIds }) { copy(blockIds = it) }
    val blockIds get() = state.now.blockIds
    val depth get() = state.now.depth

    fun createBlock(block: LayoutBlock) {
        model.addBlock(block, containerId, blockIds.size)
    }

    fun addBlock(blockId: Uuid, index: Int = blockIds.size) {
        val newBlockIds = blockIdsField.now.toMutableList()
        newBlockIds.add(index, blockId)
        blockIdsField.set(newBlockIds)
    }

    fun removeBlock(blockId: Uuid) {
        val newBlockIds = blockIdsField.now.toMutableList()
        newBlockIds.remove(blockId)
        blockIdsField.set(newBlockIds)
    }
}

data class ContainerState(
    val name: String,
    val blockIds: List<Uuid>,
    val depth: Int,
)