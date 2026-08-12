package streetlight.web.model

import koala.model.MutableTap
import koala.model.tapOf
import koala.model.mutableTapOf
import koala.model.storeOf
import streetlight.model.data.LayoutBlock
import streetlight.model.data.LayoutContainer
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class BlockEditor(
    val blockId: Uuid,
    block: LayoutBlock,
    containerIds: List<ContainerKey>?,
    val model: LayoutEditor,
) {
    private val state = storeOf(BlockState(block, containerIds))
    val blockField = state.mutableTapOf({ it.block }) { copy(block = it) }
    // val containerIdsField = state.mutableFieldOf({ it.containerKeys!! }) { copy(containerKeys = it) }
    val refreshField = state.tapOf { it.refreshAt }
    val containerKeys get() = state.now.containerKeys

    inline fun <reified T : LayoutBlock, V> mutableFieldOf(
        crossinline getter: (T) -> V,
        crossinline setter: T.(V) -> T
    ): MutableTap<V> = blockField.mutableTapOf(
        readValue = { getter(it as T) },
        writeValue = { value -> (this as T).setter(value) }
    )

    fun addBlockAbove(block: LayoutBlock) {
        model.addBlockAbove(blockId, block)
    }

    fun renameContainer(containerId: Uuid, name: String) {
        state.set {
            copy(
                containerKeys = containerKeys?.map { if (it.id == containerId) it.copy(name = name) else it },
                refreshAt = Clock.System.now()
            )
        }
    }

    fun addContainer(container: LayoutContainer) {
        val key = model.createContainer(blockId, container)
        state.set {
            copy(
                containerKeys = (containerKeys ?: emptyList()) + key,
                refreshAt = Clock.System.now()
            )
        }
    }

    fun removeContainer(containerId: Uuid) {
        state.set {
            copy(
                containerKeys = containerKeys?.filter { it.id != containerId },
                refreshAt = Clock.System.now()
            )
        }
    }

    fun refresh() {
        state.set { copy(refreshAt = Clock.System.now()) }
    }
}

data class BlockState(
    val block: LayoutBlock,
    val containerKeys: List<ContainerKey>?,
    val refreshAt: Instant = Instant.DISTANT_PAST,
)