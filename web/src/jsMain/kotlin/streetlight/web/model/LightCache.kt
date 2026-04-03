package streetlight.web.model

import koala.dom.setStorageOf
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.LightEdit
import kotlin.collections.minus
import kotlin.collections.plus

class LightCache<Id, Item>(
    private val cacheKey: String,
    val idToString: (Id) -> String,
    val stringToId: (String) -> Id,
    private val lightEdit: suspend (LightEdit) -> Boolean?,
    private val readRemoteLights: suspend () -> Set<Id>?,
    private val readRemoteItems: suspend (List<Id>) -> List<Item>?,
    private val scope: CoroutineScope,
    private val config: SiteConfig,
) {
    private val state = storeOf(LightCacheState<Id, Item>())
    val stateNow get() = state.now
    val stateFlow = state.flow
    val lightFlow = stateFlow.mapDistinct { it.lights }
    val itemsFlow = stateFlow.mapDistinct { it.items }

    private var lights by setStorageOf(cacheKey, idToString, stringToId)

    init {
        scope.launch {
            launch {
                val lights = readLights()
                state.set { it.copy(lights = lights) }
            }
            launch {
                lightFlow.collect { light ->
                    val items = when (light.isEmpty()) {
                        true -> emptyList()
                        else -> readRemoteItems(light.toList()) ?: emptyList() // td: fail message
                    }
                    state.set { it.copy(items = items) }
                }
            }
        }
    }

    fun toggleLight(id: Id) {
        val isStar = stateNow.lights.contains(id)
        when (isStar) {
            true -> removeLight(id)
            else -> addLight(id)
        }
    }

    fun addLight(id: Id) = editLight(id, true)
    fun removeLight(id: Id) = editLight(id, false)

    private fun editLight(id: Id, isLit: Boolean) {
        when (config.stateNow.lightSync) {
            true -> {
                scope.launch {
                    val edit = LightEdit(idToString(id), true)
                    val isSuccess = lightEdit(edit) ?: return@launch // td: ui message
                    if (isSuccess)
                        editState(id, isLit)
                }
            }
            else -> {
                when (isLit) {
                    true -> lights += id
                    else -> lights -= id
                }
                editState(id, isLit)
            }
        }
    }

    private fun editState(id: Id, isLit: Boolean) {
        when (isLit) {
            true -> state.set { it.copy(lights = it.lights + id) }
            else -> state.set { it.copy(lights = it.lights - id) }
        }
    }

    private suspend fun readLights() = when(config.stateNow.lightSync) {
        true -> readRemoteLights() ?: emptySet()
        else -> lights
    }
}

data class LightCacheState<Id, Item>(
    val lights: Set<Id> = emptySet(),
    val items: List<Item> = emptyList(),
)