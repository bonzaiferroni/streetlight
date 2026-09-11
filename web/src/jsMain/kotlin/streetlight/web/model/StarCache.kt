package streetlight.web.model

import kampfire.model.Messenger
import kampfire.model.Outcome
import kampfire.model.toDataOr
import koala.dom.setStorageOf
import koala.model.dedup
import kampfire.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.LightEdit
import streetlight.model.data.EditLightRequest
import streetlight.model.data.StarType
import streetlight.model.data.MultiLightEdit
import kotlin.collections.minus
import kotlin.collections.plus
import kotlin.uuid.Uuid

class StarCache<Id, Item>(
    val starType: StarType,
    private val cacheKey: String,
    val idToUuid: (Id) -> Uuid,
    val uuidToId: (Uuid) -> Id,
    val itemToId: (Item) -> Id,
    private val starLinkEdit: suspend (EditLightRequest) -> Outcome<Boolean>,
    private val readRemoteLights: suspend () -> Outcome<List<Id>>,
    private val readRemoteItems: suspend (List<Id>) -> Outcome<List<Item>>,
    private val onError: Messenger,
    private val scope: CoroutineScope,
    private val gate: SessionGate,
) {
    private val state = storeOf(LightCacheState<Id, Item>())
    val stateNow get() = state.now
    val stateFlow = state.flow
    val lightsFlow = stateFlow.dedup { it.lights }
    val itemsFlow = stateFlow.dedup { it.items }

    private val idToString: (Id) -> String = { idToUuid(it).toString() }
    private val stringToId: (String) -> Id = { uuidToId(Uuid.parse(it)) }

    private var cachedLights by setStorageOf(cacheKey, idToString, stringToId)

    init {
        scope.launch {
            launch {
                gate.signedInFlow.collect { isSignedIn ->
                    if (isSignedIn) {
                        if (cachedLights.isNotEmpty()) {
                            val request = MultiLightEdit(cachedLights.map { LightEdit(idToUuid(it), true, starType) })
                            // send lights cached while signed out
                            starLinkEdit(request)
                            cachedLights = emptySet()
                        }
                        val lights = readRemoteLights().toDataOr(onError) { return@collect }
                        state.set { copy(lights = lights.toSet())}
                    }
                }
            }
            launch {
                lightsFlow.collect { lights ->
                    val newIds = lights.filter { lightId -> stateNow.items.none { itemToId(it) == lightId } }
                    val newItems = when (newIds.isEmpty()) {
                        true -> emptyList()
                        else -> readRemoteItems(newIds.toList()).toDataOr(onError) { return@collect }
                    }
                    state.set { copy(items = items.filter { item -> lights.contains(itemToId(item)) } + newItems) }
                }
            }
        }
    }

    fun toggleLight(id: Id): Boolean {
        val isLitNow = stateNow.lights.contains(id)
        when (isLitNow) {
            true -> removeLight(id)
            else -> addLight(id)
        }
        return !isLitNow
    }

    fun addLight(id: Id) = editLight(id, true)
    fun removeLight(id: Id) = editLight(id, false)

    private fun editLight(id: Id, isLit: Boolean) {
        when (gate.stateNow.isSignedIn) {
            true -> {
                scope.launch {
                    val edit = LightEdit(idToUuid(id), isLit, starType)
                    val isSuccess = starLinkEdit(edit).toDataOr(onError) { return@launch }
                    if (isSuccess)
                        editState(id, isLit)
                }
            }
            else -> {
                when (isLit) {
                    true -> cachedLights += id
                    else -> cachedLights -= id
                }
                editState(id, isLit)
            }
        }
    }

    private fun editState(id: Id, isLit: Boolean) {
        when (isLit) {
            true -> state.set { copy(lights = lights + id) }
            else -> state.set { copy(lights = lights - id) }
        }
    }
}

data class LightCacheState<Id, Item>(
    val lights: Set<Id> = emptySet(),
    val items: List<Item> = emptyList(),
)